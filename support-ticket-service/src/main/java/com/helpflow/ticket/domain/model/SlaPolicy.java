package com.helpflow.ticket.domain.model;

import com.helpflow.ticket.domain.exception.BusinessRuleException;
import com.helpflow.ticket.domain.model.enums.Priority;

import java.time.Duration;
import java.time.Instant;

/** Política de SLA por prioridad (configurable por ADMIN). Inmutable. */
public record SlaPolicy(Priority priority, int responseHours, int resolutionHours) {

    public SlaPolicy {
        DomainValidation.requireNonNull(priority, "priority");
        if (responseHours < 1) {
            throw new BusinessRuleException("Las horas de respuesta deben ser mayores que 0");
        }
        if (resolutionHours <= responseHours) {
            throw new BusinessRuleException("Las horas de resolución deben ser mayores que las de respuesta");
        }
    }

    /** Fecha límite de SLA de un ticket creado en {@code start}. */
    public Instant resolutionDeadlineFrom(Instant start) {
        return start.plus(Duration.ofHours(resolutionHours));
    }

    public SlaPolicy withHours(int newResponseHours, int newResolutionHours) {
        return new SlaPolicy(priority, newResponseHours, newResolutionHours);
    }
}
