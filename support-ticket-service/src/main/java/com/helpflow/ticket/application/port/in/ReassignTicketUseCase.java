package com.helpflow.ticket.application.port.in;

import com.helpflow.ticket.domain.model.CurrentUser;
import com.helpflow.ticket.domain.model.Ticket;

public interface ReassignTicketUseCase {

    Ticket reassign(ReassignTicketCommand command, CurrentUser actor);

    record ReassignTicketCommand(Long ticketId, Long technicianId, String observation) {
    }
}
