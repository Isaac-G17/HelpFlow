package com.helpflow.ticket.domain.model;

import java.time.Instant;

/** Entrada inmutable de la trazabilidad de un ticket. performedBy es null cuando actúa el sistema. */
public record TicketHistory(
        Long id,
        Long ticketId,
        TicketStatus fromStatus,
        TicketStatus toStatus,
        HistoryAction action,
        String observation,
        Long performedBy,
        Instant createdAt
) {

    public static TicketHistory of(Long ticketId, TicketStatus from, TicketStatus to, HistoryAction action,
                                   String observation, Long performedBy, Instant now) {
        DomainValidation.requireNonNull(ticketId, "ticketId");
        DomainValidation.requireNonNull(to, "toStatus");
        DomainValidation.requireNonNull(action, "action");
        return new TicketHistory(null, ticketId, from, to, action,
                DomainValidation.requireText(observation, "observation"), performedBy, now);
    }
}
