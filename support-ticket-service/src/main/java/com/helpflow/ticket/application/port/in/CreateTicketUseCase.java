package com.helpflow.ticket.application.port.in;

import com.helpflow.ticket.domain.model.Category;
import com.helpflow.ticket.domain.model.CurrentUser;
import com.helpflow.ticket.domain.model.Priority;
import com.helpflow.ticket.domain.model.Ticket;

public interface CreateTicketUseCase {

    /** Registra el ticket (ABIERTO), consulta dispatch y aplica la asignación inicial. */
    Ticket create(CreateTicketCommand command, CurrentUser actor);

    record CreateTicketCommand(String title, String description, Category category, Priority priority,
                               String sede, String affectedAsset) {
    }
}
