package com.helpflow.ticket.domain.model;

import com.helpflow.ticket.domain.model.enums.AssignmentOrigin;
import com.helpflow.ticket.domain.model.enums.Availability;

import java.time.Instant;

/** Registro de qué técnico atiende un ticket y por qué. Solo una asignación activa por ticket. */
public class Assignment {

    private final Long id;
    private final Long ticketId;
    private final Long technicianId;
    private final Availability availability;
    private final Integer estimatedHours;
    private final AssignmentOrigin origin;
    private boolean active;
    private final Instant assignedAt;

    public Assignment(Long id, Long ticketId, Long technicianId, Availability availability, Integer estimatedHours,
                      AssignmentOrigin origin, boolean active, Instant assignedAt) {
        this.id = id;
        this.ticketId = ticketId;
        this.technicianId = technicianId;
        this.availability = availability;
        this.estimatedHours = estimatedHours;
        this.origin = origin;
        this.active = active;
        this.assignedAt = assignedAt;
    }

    /** El técnico sugerido por dispatch cumplió las reglas. */
    public static Assignment fromDispatch(Long ticketId, Long technicianId, DispatchSuggestion suggestion, Instant now) {
        return new Assignment(null, ticketId, technicianId, suggestion.availability(), suggestion.estimatedHours(),
                AssignmentOrigin.DISPATCH, true, now);
    }

    /** Técnico local elegido porque el sugerido no cumplió (no hay estimación propia). */
    public static Assignment fallback(Long ticketId, Long technicianId, Instant now) {
        return new Assignment(null, ticketId, technicianId, null, null, AssignmentOrigin.FALLBACK, true, now);
    }

    public static Assignment manual(Long ticketId, Long technicianId, Instant now) {
        return new Assignment(null, ticketId, technicianId, null, null, AssignmentOrigin.MANUAL, true, now);
    }

    public void deactivate() {
        this.active = false;
    }

    public Long getId() {
        return id;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public Long getTechnicianId() {
        return technicianId;
    }

    public Availability getAvailability() {
        return availability;
    }

    public Integer getEstimatedHours() {
        return estimatedHours;
    }

    public AssignmentOrigin getOrigin() {
        return origin;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }
}
