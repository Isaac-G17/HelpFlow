package com.helpflow.ticket.application.port.in;

import com.helpflow.ticket.domain.model.CurrentUser;
import com.helpflow.ticket.domain.model.Ticket;

public interface AutoAssignTicketUseCase {

    /** Reintenta la consulta a dispatch para un ticket ABIERTO. Si dispatch falla -> 503. */
    Ticket autoAssign(Long ticketId, CurrentUser actor);
}
