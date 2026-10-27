package com.helpflow.ticket.domain.model.enums;

public enum Priority {
    BAJA,
    MEDIA,
    ALTA,
    CRITICA;

    /**
     * Prioridad siguiente al escalar un ticket.
     *
     * TODO (tú): implementar.
     *  1. BAJA -> MEDIA, MEDIA -> ALTA, ALTA -> CRITICA.
     *  2. CRITICA ya es la máxima: devuelve CRITICA (no lanza excepción).
     *  Pista: puedes usar ordinal() y values(), o un switch expression.
     */
    public Priority next() {

        return switch (this) {
            case BAJA -> MEDIA;
            case MEDIA -> ALTA;
            case ALTA, CRITICA -> CRITICA;
        };
    }
}
