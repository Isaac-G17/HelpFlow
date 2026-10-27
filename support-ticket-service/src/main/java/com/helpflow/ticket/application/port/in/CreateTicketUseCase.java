package com.helpflow.ticket.application.port.in;

import com.helpflow.ticket.domain.model.CurrentUser;
import com.helpflow.ticket.domain.model.Ticket;
import com.helpflow.ticket.domain.model.enums.Category;
import com.helpflow.ticket.domain.model.enums.Priority;

public interface CreateTicketUseCase {

    /** Registra el ticket (ABIERTO), consulta dispatch y aplica la asignación inicial. */
    Ticket create(CreateTicketCommand command, CurrentUser actor);

    record CreateTicketCommand(String title, String description, Category category, Priority priority,
                               String sede, String affectedAsset) {
    }
}
