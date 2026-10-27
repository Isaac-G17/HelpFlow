package com.helpflow.ticket.application.port.in;

import com.helpflow.ticket.domain.model.DispatchSuggestion;
import com.helpflow.ticket.domain.model.Ticket;

public interface AssignTicketUseCase {

    /** Operación crítica: asignación + historial + carga del técnico en UNA transacción. */
    Ticket assign(AssignTicketCommand command);

    /**
     * @param suggestion  respuesta de dispatch (no null)
     * @param performedBy usuario que disparó la asignación (null = sistema)
     */
    record AssignTicketCommand(Long ticketId, DispatchSuggestion suggestion, Long performedBy) {
    }
}
