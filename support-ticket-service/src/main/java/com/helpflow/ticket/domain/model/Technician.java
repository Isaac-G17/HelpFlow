package com.helpflow.ticket.domain.model;

import com.helpflow.ticket.domain.exception.BusinessRuleException;
import com.helpflow.ticket.domain.exception.InactiveUserException;
import com.helpflow.ticket.domain.exception.TechnicianCapacityExceededException;

/**
 * Perfil de soporte de un usuario con rol TECNICO.
 * activeTickets funciona como un "stock": takeTicket() lo sube y releaseTicket() lo baja.
 * version permite el bloqueo optimista (@Version en la entidad JPA): si dos asignaciones
 * concurrentes toman al mismo técnico, la segunda falla con 409.
 */
public class Technician {

    private final Long id;
    private final String code;
    private final Long userId;
    private Category specialty;
    private String sede;
    private AccountStatus status;
    private int maxActiveTickets;
    private int activeTickets;
    private final Long version;

    public Technician(Long id, String code, Long userId, Category specialty, String sede, AccountStatus status,
                      int maxActiveTickets, int activeTickets, Long version) {
        this.id = id;
        this.code = code;
        this.userId = userId;
        this.specialty = specialty;
        this.sede = sede;
        this.status = status;
        this.maxActiveTickets = maxActiveTickets;
        this.activeTickets = activeTickets;
        this.version = version;
    }

    public static Technician create(String code, Long userId, Category specialty, String sede, int maxActiveTickets) {
        if (maxActiveTickets < 1) {
            throw new BusinessRuleException("La carga máxima de tickets debe ser mayor que 0");
        }
        return new Technician(null,
                DomainValidation.requireText(code, "code"),
                DomainValidation.requireNonNull(userId, "userId"),
                DomainValidation.requireNonNull(specialty, "specialty"),
                DomainValidation.normalizeSede(sede),
                AccountStatus.ACTIVO,
                maxActiveTickets,
                0,
                null);
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVO;
    }

    /**
     * Compatibilidad entre la categoría del ticket y la especialidad del técnico.
     *
     * TODO (tú): implementar.
     *  1. Si la categoría del ticket es OTRO, cualquier especialidad la puede atender -> true.
     *  2. En otro caso, la especialidad del técnico debe ser igual a la categoría.
     */
    public boolean canAttend(Category category) {
        throw new UnsupportedOperationException("TODO: Technician.canAttend");
    }

    /**
     * TODO (tú): true si todavía puede recibir otro ticket (activeTickets < maxActiveTickets).
     */
    public boolean hasCapacity() {
        throw new UnsupportedOperationException("TODO: Technician.hasCapacity");
    }

    /**
     * El técnico toma un ticket (sube su carga). Equivalente a "descontar stock".
     *
     * TODO (tú): implementar.
     *  1. Si no está activo -> InactiveUserException("El técnico ... está INACTIVO").
     *  2. Si no tiene capacidad -> TechnicianCapacityExceededException(code, maxActiveTickets).
     *  3. activeTickets++.
     */
    public void takeTicket() {
        throw new UnsupportedOperationException("TODO: Technician.takeTicket");
    }

    /**
     * El técnico libera un ticket (RESUELTO, CANCELADO o reasignado a otro).
     *
     * TODO (tú): implementar.
     *  1. Si activeTickets ya es 0 -> BusinessRuleException (nunca debe quedar negativo).
     *  2. activeTickets--.
     */
    public void releaseTicket() {
        throw new UnsupportedOperationException("TODO: Technician.releaseTicket");
    }

    /** Edición del perfil de soporte (ADMIN). */
    public void updateProfile(Category specialty, String sede, int maxActiveTickets, AccountStatus status) {
        if (maxActiveTickets < 1) {
            throw new BusinessRuleException("La carga máxima de tickets debe ser mayor que 0");
        }
        if (maxActiveTickets < activeTickets) {
            throw new BusinessRuleException("La carga máxima (" + maxActiveTickets
                    + ") no puede ser menor que los tickets activos actuales (" + activeTickets + ")");
        }
        this.specialty = DomainValidation.requireNonNull(specialty, "specialty");
        this.sede = DomainValidation.normalizeSede(sede);
        this.maxActiveTickets = maxActiveTickets;
        this.status = DomainValidation.requireNonNull(status, "status");
    }

    /** Lanza si el técnico está inactivo (útil en reasignaciones manuales). */
    public void ensureActive() {
        if (!isActive()) {
            throw new InactiveUserException("El técnico " + code + " está INACTIVO");
        }
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Long getUserId() {
        return userId;
    }

    public Category getSpecialty() {
        return specialty;
    }

    public String getSede() {
        return sede;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public int getMaxActiveTickets() {
        return maxActiveTickets;
    }

    public int getActiveTickets() {
        return activeTickets;
    }

    public Long getVersion() {
        return version;
    }
}
