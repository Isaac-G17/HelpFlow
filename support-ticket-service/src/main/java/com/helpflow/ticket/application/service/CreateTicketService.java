package com.helpflow.ticket.application.service;

import com.helpflow.ticket.application.port.in.AssignTicketUseCase;
import com.helpflow.ticket.application.port.in.CreateTicketUseCase;
import com.helpflow.ticket.application.port.out.TechnicianDispatchPort;
import com.helpflow.ticket.domain.model.CurrentUser;
import com.helpflow.ticket.domain.model.Ticket;
import org.springframework.stereotype.Service;

/**
 * Orquesta el flujo obligatorio del enunciado. NO es @Transactional a propósito:
 * no se debe retener una conexión de BD mientras se espera la respuesta HTTP de dispatch.
 *
 *   [tx 1] TicketRegistrationService.open  -> ticket ABIERTO + historial CREATED
 *   [sin tx] TechnicianDispatchPort        -> técnico sugerido + horas estimadas
 *   [tx 2] AssignTicketUseCase.assign      -> asignación + historial + carga del técnico
 */
@Service
public class CreateTicketService implements CreateTicketUseCase {

    private final TicketRegistrationService registration;
    private final TechnicianDispatchPort dispatchPort;
    private final AssignTicketUseCase assignTicketUseCase;

    public CreateTicketService(TicketRegistrationService registration, TechnicianDispatchPort dispatchPort,
                               AssignTicketUseCase assignTicketUseCase) {
        this.registration = registration;
        this.dispatchPort = dispatchPort;
        this.assignTicketUseCase = assignTicketUseCase;
    }

    /**
     * TODO (tú): implementar.
     *  1. Ticket ticket = registration.open(command, actor);
     *  2. try {
     *        DispatchSuggestion suggestion = dispatchPort.requestTechnician(ticket.getId(), ticket.getCategory(), ticket.getPriority());
     *        return assignTicketUseCase.assign(new AssignTicketCommand(ticket.getId(), suggestion, actor.id()));
     *     } catch (TechnicianDispatchUnavailableException e) {
     *        // Decisión aprobada: la creación NO falla si dispatch está caído (el ticket no se pierde).
     *        registration.recordDispatchFailure(ticket.getId(), e.getMessage());
     *        return registration.reload(ticket.getId());   // queda ABIERTO; un ADMIN puede usar auto-assign
     *     }
     *  Pregunta de defensa: ¿por qué este método no lleva @Transactional?
     */
    @Override
    public Ticket create(CreateTicketCommand command, CurrentUser actor) {
        throw new UnsupportedOperationException("TODO: CreateTicketService.create");
    }
}
