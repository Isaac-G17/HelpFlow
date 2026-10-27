package com.helpflow.ticket.application.port.in;

import com.helpflow.ticket.domain.model.CurrentUser;
import com.helpflow.ticket.domain.model.Ticket;
import com.helpflow.ticket.domain.model.TicketStatus;

public interface ChangeTicketStatusUseCase {

    Ticket changeStatus(ChangeStatusCommand command, CurrentUser actor);

    record ChangeStatusCommand(Long ticketId, TicketStatus target, String observation) {
    }
}
