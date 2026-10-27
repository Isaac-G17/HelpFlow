package com.helpflow.ticket.infrastructure.adapter.in.rest;

import com.helpflow.ticket.application.port.in.AutoAssignTicketUseCase;
import com.helpflow.ticket.application.port.in.ChangeTicketStatusUseCase;
import com.helpflow.ticket.application.port.in.ChangeTicketStatusUseCase.ChangeStatusCommand;
import com.helpflow.ticket.application.port.in.CreateTicketUseCase;
import com.helpflow.ticket.application.port.in.EscalateTicketsUseCase;
import com.helpflow.ticket.application.port.in.QueryTicketUseCase;
import com.helpflow.ticket.application.port.in.ReassignTicketUseCase;
import com.helpflow.ticket.application.port.in.ReassignTicketUseCase.ReassignTicketCommand;
import com.helpflow.ticket.domain.model.CurrentUser;
import com.helpflow.ticket.domain.model.PageQuery;
import com.helpflow.ticket.domain.model.Priority;
import com.helpflow.ticket.domain.model.TicketFilter;
import com.helpflow.ticket.domain.model.TicketStatus;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.PageResponse;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.TicketDtos.ChangeStatusRequest;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.TicketDtos.CreateTicketRequest;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.TicketDtos.EscalationResponse;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.TicketDtos.HistoryResponse;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.TicketDtos.ReassignRequest;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.TicketDtos.TicketResponse;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.TicketDtos.TicketSummaryResponse;
import com.helpflow.ticket.infrastructure.adapter.in.rest.mapper.RestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * El controller solo traduce HTTP <-> casos de uso. Aquí se valida el ROL (@PreAuthorize);
 * la PROPIEDAD (que el ticket sea suyo o esté asignado a él) se valida en los casos de uso.
 */
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets")
public class TicketController {

    private final CreateTicketUseCase createTicketUseCase;
    private final QueryTicketUseCase queryTicketUseCase;
    private final ChangeTicketStatusUseCase changeTicketStatusUseCase;
    private final AutoAssignTicketUseCase autoAssignTicketUseCase;
    private final ReassignTicketUseCase reassignTicketUseCase;
    private final EscalateTicketsUseCase escalateTicketsUseCase;
    private final RestMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USUARIO','ADMIN')")
    @Operation(summary = "Registrar ticket: queda ABIERTO, consulta dispatch y aplica la asignación inicial")
    public TicketResponse create(@AuthenticationPrincipal CurrentUser currentUser,
                                 @Valid @RequestBody CreateTicketRequest request) {
        return mapper.toResponse(createTicketUseCase.create(mapper.toCommand(request), currentUser));
    }

    @GetMapping
    @Operation(summary = "Listar tickets: USUARIO los suyos, TECNICO los asignados, ADMIN todos")
    public PageResponse<TicketSummaryResponse> list(@AuthenticationPrincipal CurrentUser currentUser,
                                                    @RequestParam(required = false) TicketStatus status,
                                                    @RequestParam(required = false) Priority priority,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        TicketFilter filter = new TicketFilter(null, null, status, priority);
        return PageResponse.from(queryTicketUseCase.list(filter, new PageQuery(page, size), currentUser),
                mapper::toResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un ticket (403 si no es suyo / no está asignado a él)")
    public TicketResponse get(@AuthenticationPrincipal CurrentUser currentUser, @PathVariable Long id) {
        return mapper.toResponse(queryTicketUseCase.getById(id, currentUser));
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Historial de cambios del ticket")
    public List<HistoryResponse> history(@AuthenticationPrincipal CurrentUser currentUser, @PathVariable Long id) {
        return queryTicketUseCase.history(id, currentUser).stream().map(mapper::toResponse).toList();
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cambiar estado con observación (reglas por rol y máquina de estados)")
    public TicketResponse changeStatus(@AuthenticationPrincipal CurrentUser currentUser, @PathVariable Long id,
                                       @Valid @RequestBody ChangeStatusRequest request) {
        return mapper.toResponse(changeTicketStatusUseCase.changeStatus(
                new ChangeStatusCommand(id, request.status(), request.observation()), currentUser));
    }

    @PostMapping("/{id}/auto-assign")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reintentar asignación con dispatch para un ticket ABIERTO (503 si dispatch no responde)")
    public TicketResponse autoAssign(@AuthenticationPrincipal CurrentUser currentUser, @PathVariable Long id) {
        return mapper.toResponse(autoAssignTicketUseCase.autoAssign(id, currentUser));
    }

    @PutMapping("/{id}/assignment")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reasignación manual a otro técnico (ADMIN)")
    public TicketResponse reassign(@AuthenticationPrincipal CurrentUser currentUser, @PathVariable Long id,
                                   @Valid @RequestBody ReassignRequest request) {
        return mapper.toResponse(reassignTicketUseCase.reassign(
                new ReassignTicketCommand(id, request.technicianId(), request.observation()), currentUser));
    }

    @PostMapping("/escalations/run")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ejecutar ya el escalamiento por SLA (también corre cada 5 min)")
    public EscalationResponse runEscalation() {
        return new EscalationResponse(escalateTicketsUseCase.escalateNearBreach());
    }
}
