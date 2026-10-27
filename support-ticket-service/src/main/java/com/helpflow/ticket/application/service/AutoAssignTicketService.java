package com.helpflow.ticket.application.service;

import com.helpflow.ticket.application.port.in.AssignTicketUseCase;
import com.helpflow.ticket.application.port.in.AutoAssignTicketUseCase;
import com.helpflow.ticket.application.port.in.AssignTicketUseCase.AssignTicketCommand;
import com.helpflow.ticket.application.port.out.TechnicianDispatchPort;
import com.helpflow.ticket.application.port.out.TicketRepositoryPort;
import com.helpflow.ticket.domain.exception.InvalidStateTransitionException;
import com.helpflow.ticket.domain.exception.ResourceNotFoundException;
import com.helpflow.ticket.domain.model.CurrentUser;
import com.helpflow.ticket.domain.model.DispatchSuggestion;
import com.helpflow.ticket.domain.model.enums.TicketStatus;
import com.helpflow.ticket.domain.model.Ticket;
import org.springframework.stereotype.Service;

/** Reintento manual (ADMIN) de la asignación por dispatch. NO es @Transactional (llama a HTTP). */
@Service
public class AutoAssignTicketService implements AutoAssignTicketUseCase {

    private final TicketRepositoryPort ticketRepository;
    private final TechnicianDispatchPort dispatchPort;
    private final AssignTicketUseCase assignTicketUseCase;

    public AutoAssignTicketService(TicketRepositoryPort ticketRepository, TechnicianDispatchPort dispatchPort,
                                   AssignTicketUseCase assignTicketUseCase) {
        this.ticketRepository = ticketRepository;
        this.dispatchPort = dispatchPort;
        this.assignTicketUseCase = assignTicketUseCase;
    }

    /**
     * TODO (tú): implementar.
     *  1. ticket = ticketRepository.findById(...) o ResourceNotFoundException.
     *  2. Si ticket.getStatus() != ABIERTO -> InvalidStateTransitionException("Solo se auto-asignan tickets ABIERTO").
     *  3. suggestion = dispatchPort.requestTechnician(...)
     *     Aquí NO captures TechnicianDispatchUnavailableException: debe llegar al handler -> 503.
     *  4. return assignTicketUseCase.assign(new AssignTicketCommand(ticketId, suggestion, actor.id())).
     */
    @Override
    public Ticket autoAssign(Long ticketId, CurrentUser actor) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));
        if (ticket.getStatus() != TicketStatus.ABIERTO) {
            throw new InvalidStateTransitionException("Solo se auto-asignan tickets ABIERTO");
        }
        DispatchSuggestion suggestion = dispatchPort.requestTechnician(
                ticket.getId(), ticket.getCategory(), ticket.getPriority());
        return assignTicketUseCase.assign(new AssignTicketCommand(ticket.getId(), suggestion, actor.id()));
    }
}
