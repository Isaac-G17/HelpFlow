package com.helpflow.ticket.domain.model;

/** Respuesta de technician-dispatch-service traducida al lenguaje del dominio. */
public record DispatchSuggestion(
        Long ticketId,
        String technicianCode,
        Availability availability,
        int estimatedHours
) {
}
