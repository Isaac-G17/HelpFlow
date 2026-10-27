package com.helpflow.ticket.domain.model.enums;

import java.util.Set;

public enum TicketStatus {
    ABIERTO,
    ASIGNADO,
    EN_PROGRESO,
    RESUELTO,
    CERRADO,
    CANCELADO;

    private static final Set<TicketStatus> ACTIVE = Set.of(ABIERTO, ASIGNADO, EN_PROGRESO);

    /** Ticket que todavía consume SLA (ni resuelto ni terminado). */
    public boolean isActive() {
        return ACTIVE.contains(this);
    }

    /** CERRADO y CANCELADO no admiten más cambios. */
    public boolean isFinal() {
        return this == CERRADO || this == CANCELADO;
    }

    public static Set<TicketStatus> activeStatuses() {
        return ACTIVE;
    }


    public boolean canTransitionTo(TicketStatus target) {
        Set<TicketStatus> allowed = switch (this) {
            case ABIERTO -> Set.of(ASIGNADO, CANCELADO);
            case ASIGNADO -> Set.of(EN_PROGRESO, CANCELADO);
            case EN_PROGRESO -> Set.of(RESUELTO, CANCELADO);
            case RESUELTO -> Set.of(CERRADO, EN_PROGRESO);
            case CERRADO, CANCELADO -> Set.of();
        };
        return allowed.contains(target);
    }
}
