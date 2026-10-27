package com.helpflow.ticket.domain.model.enums;

/** De dónde salió el técnico asignado. */
public enum AssignmentOrigin {
    /** El técnico sugerido por technician-dispatch-service cumplió las reglas internas. */
    DISPATCH,
    /** El sugerido no cumplió y se eligió otro técnico local compatible. */
    FALLBACK,
    /** Reasignación manual de un ADMIN. */
    MANUAL
}
