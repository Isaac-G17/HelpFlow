   
   
   package com.helpflow.ticket.domain.model;

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

    /**
     * ¿Es válida la transición this -> target?
     *
     * TODO (tú): implementar la máquina de estados aprobada:
     *   ABIERTO     -> ASIGNADO | CANCELADO
     *   ASIGNADO    -> EN_PROGRESO | CANCELADO
     *   EN_PROGRESO -> RESUELTO | CANCELADO
     *   RESUELTO    -> CERRADO | EN_PROGRESO   (reapertura)
     *   CERRADO, CANCELADO -> ninguna (finales)
     *
     *  Pista: un switch expression sobre this que devuelva un Set.of(...) y luego contains(target).
     *  Ojo: la transición a sí mismo (ABIERTO -> ABIERTO) NO es válida.
     */
    public boolean canTransitionTo(TicketStatus target) {
        throw new UnsupportedOperationException("TODO: TicketStatus.canTransitionTo");
    }
}
