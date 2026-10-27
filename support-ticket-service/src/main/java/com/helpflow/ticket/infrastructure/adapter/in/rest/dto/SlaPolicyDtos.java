package com.helpflow.ticket.infrastructure.adapter.in.rest.dto;

import com.helpflow.ticket.domain.model.enums.Priority;
import com.helpflow.ticket.infrastructure.adapter.in.rest.validation.ValidSlaPolicy;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;

public final class SlaPolicyDtos {

    private SlaPolicyDtos() {
    }

    /** Validación cruzada: resolutionHours > responseHours (ver {@link ValidSlaPolicy}). */
    @ValidSlaPolicy
    public record SlaPolicyRequest(
            @Positive @Max(720) int responseHours,
            @Positive @Max(720) int resolutionHours
    ) {
    }

    public record SlaPolicyResponse(Priority priority, int responseHours, int resolutionHours) {
    }
}
