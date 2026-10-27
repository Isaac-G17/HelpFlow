package com.helpflow.ticket.domain.model;

/** Criterios de búsqueda de tickets. Un campo null significa "sin filtrar por ese campo". */
public record TicketFilter(Long requesterId, Long technicianId, TicketStatus status, Priority priority) {

    public TicketFilter withRequester(Long requesterId) {
        return new TicketFilter(requesterId, technicianId, status, priority);
    }

    public TicketFilter withTechnician(Long technicianId) {
        return new TicketFilter(requesterId, technicianId, status, priority);
    }
}
