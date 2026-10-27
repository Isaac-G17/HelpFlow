package com.helpflow.ticket.infrastructure.adapter.in.rest;

import com.helpflow.ticket.application.port.in.ManageSlaPolicyUseCase;
import com.helpflow.ticket.application.port.in.ManageSlaPolicyUseCase.UpdateSlaPolicyCommand;
import com.helpflow.ticket.domain.model.Priority;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.SlaPolicyDtos.SlaPolicyRequest;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.SlaPolicyDtos.SlaPolicyResponse;
import com.helpflow.ticket.infrastructure.adapter.in.rest.mapper.RestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sla-policies")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Configuración de SLA (ADMIN)")
public class SlaPolicyController {

    private final ManageSlaPolicyUseCase manageSlaPolicyUseCase;
    private final RestMapper mapper;

    @GetMapping
    @Operation(summary = "Políticas de SLA por prioridad")
    public List<SlaPolicyResponse> list() {
        return manageSlaPolicyUseCase.list().stream().map(mapper::toResponse).toList();
    }

    @PutMapping("/{priority}")
    @Operation(summary = "Configurar horas de respuesta y resolución (aplica a tickets nuevos)")
    public SlaPolicyResponse update(@PathVariable Priority priority, @Valid @RequestBody SlaPolicyRequest request) {
        return mapper.toResponse(manageSlaPolicyUseCase.update(
                new UpdateSlaPolicyCommand(priority, request.responseHours(), request.resolutionHours())));
    }
}
