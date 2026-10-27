package com.helpflow.ticket.application.port.out;

import com.helpflow.ticket.domain.model.enums.AssignmentOrigin;
import com.helpflow.ticket.domain.model.enums.Category;
import com.helpflow.ticket.domain.model.enums.Priority;
import com.helpflow.ticket.domain.model.enums.TicketStatus;

import java.time.Duration;

/** Métricas de negocio. El adapter usa Micrometer; la aplicación no lo conoce. */
public interface TicketMetricsPort {

    void ticketCreated(Category category, Priority priority);

    void ticketAssigned(AssignmentOrigin origin);

    void statusChanged(TicketStatus target);

    void firstResponse(Duration elapsed);

    void resolution(Duration elapsed);

    void ticketsEscalated(int count);
}
