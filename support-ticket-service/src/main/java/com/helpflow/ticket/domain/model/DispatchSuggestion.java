package com.helpflow.ticket.domain.model;

import com.helpflow.ticket.domain.model.enums.Availability;

/** Respuesta de technician-dispatch-service traducida al lenguaje del dominio. */
public record DispatchSuggestion(
        Long ticketId,
        String technicianCode,
        Availability availability,
        int estimatedHours
) {
}
