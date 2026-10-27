package com.helpflow.ticket.domain.model;

import com.helpflow.ticket.domain.model.enums.Category;
import com.helpflow.ticket.domain.model.enums.Priority;
import com.helpflow.ticket.domain.model.enums.TicketStatus;

import java.time.Instant;

/**
 * Modelo de lectura para los listados: incluye datos del solicitante y del técnico,
 * por eso la consulta usa @EntityGraph (evita N+1).
 */
public record TicketSummary(
        Long id,
        String title,
        Category category,
        Priority priority,
        TicketStatus status,
        String sede,
        boolean escalated,
        Instant createdAt,
        Instant slaDeadline,
        Long requesterId,
        String requesterName,
        Long technicianId,
        String technicianCode
) {
}
