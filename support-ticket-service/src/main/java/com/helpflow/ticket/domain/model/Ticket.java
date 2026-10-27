package com.helpflow.ticket.domain.model;

import com.helpflow.ticket.domain.exception.BusinessRuleException;
import com.helpflow.ticket.domain.exception.ForbiddenOperationException;
import com.helpflow.ticket.domain.exception.InvalidStateTransitionException;
import com.helpflow.ticket.domain.model.enums.Category;
import com.helpflow.ticket.domain.model.enums.Priority;
import com.helpflow.ticket.domain.model.enums.Role;
import com.helpflow.ticket.domain.model.enums.TicketStatus;

import java.time.Duration;
import java.time.Instant;

/**
 * Ticket de soporte: el agregado central. Aquí viven las reglas del ciclo de vida.
 * Relaciones por id (requesterId, technicianId): el dominio no navega entidades JPA.
 */
public class Ticket {

    private final Long id;
    private final Long requesterId;
    private final String title;
    private final String description;
    private final Category category;
    private Priority priority;
    private final String sede;
    private final String affectedAsset;
    private TicketStatus status;
    private Long technicianId;
    private boolean escalated;
    private final Instant createdAt;
    private final Instant slaDeadline;
    private Instant firstResponseAt;
    private Instant resolvedAt;
    private final Long version;

    public Ticket(Long id, Long requesterId, String title, String description, Category category, Priority priority,
                  String sede, String affectedAsset, TicketStatus status, Long technicianId, boolean escalated,
                  Instant createdAt, Instant slaDeadline, Instant firstResponseAt, Instant resolvedAt, Long version) {
        this.id = id;
        this.requesterId = requesterId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.sede = sede;
        this.affectedAsset = affectedAsset;
        this.status = status;
        this.technicianId = technicianId;
        this.escalated = escalated;
        this.createdAt = createdAt;
        this.slaDeadline = slaDeadline;
        this.firstResponseAt = firstResponseAt;
        this.resolvedAt = resolvedAt;
        this.version = version;
    }

    /**
     * Fábrica: registra un ticket nuevo en estado ABIERTO.
     *
     * TODO (tú): implementar.
     *  1. requester.ensureActive()  -> regla "usuario ACTIVO para registrar tickets" (422 si no).
     *  2. Valida que category y priority no sean null (DomainValidation.requireNonNull).
     *  3. Valida que la política recibida corresponda a la prioridad del ticket
     *     (policy.priority() == priority), si no -> BusinessRuleException.
     *  4. slaDeadline = policy.resolutionDeadlineFrom(now).
     *  5. Devuelve new Ticket(null, requester.getId(), title (requireText), description (requireText),
     *     category, priority, DomainValidation.normalizeSede(sede), affectedAsset (requireText),
     *     TicketStatus.ABIERTO, null, false, now, slaDeadline, null, null, null).
     *  Mira User.create(...) como ejemplo del patrón.
     */
    public static Ticket open(User requester, String title, String description, Category category, Priority priority,
                              String sede, String affectedAsset, SlaPolicy policy, Instant now) {
        throw new UnsupportedOperationException("TODO: Ticket.open");
    }

    /**
     * Asignación inicial (desde ABIERTO).
     *
     * TODO (tú): implementar.
     *  1. Si status no es ABIERTO -> InvalidStateTransitionException(status, ASIGNADO).
     *  2. Si el técnico no puede atender la categoría (technician.canAttend) -> BusinessRuleException.
     *  3. technician.takeTicket()   (valida activo y capacidad, y sube su carga).
     *  4. this.technicianId = technician.getId(); this.status = ASIGNADO.
     */
    public void assignTo(Technician technician) {
        throw new UnsupportedOperationException("TODO: Ticket.assignTo");
    }

    /**
     * Reasignación manual (ADMIN) a otro técnico.
     *
     * TODO (tú): implementar.
     *  1. Solo se permite si status es ASIGNADO o EN_PROGRESO; si no -> InvalidStateTransitionException
     *     (mensaje claro, p. ej. "Solo se reasignan tickets ASIGNADO o EN_PROGRESO").
     *  2. Si newTechnician es el mismo técnico actual -> BusinessRuleException.
     *  3. Si no puede atender la categoría -> BusinessRuleException.
     *  4. newTechnician.takeTicket(); previous.releaseTicket()  (previous = técnico actual, lo carga el caso de uso).
     *  5. technicianId = nuevo; status = ASIGNADO (vuelve a empezar la atención).
     */
    public void reassignTo(Technician previous, Technician newTechnician) {
        throw new UnsupportedOperationException("TODO: Ticket.reassignTo");
    }

    /**
     * Cambio de estado manual (PATCH /status). Devuelve el estado anterior (para el historial).
     * La PROPIEDAD (¿es su ticket? ¿está asignado a él?) la valida el caso de uso;
     * aquí se valida la máquina de estados y qué ROL puede hacer cada transición.
     *
     * TODO (tú): implementar.
     *  1. target == ASIGNADO no se permite por aquí (solo por asignación) -> InvalidStateTransitionException.
     *  2. Si !status.canTransitionTo(target) -> InvalidStateTransitionException(status, target).
     *  3. Permisos por rol (si no -> ForbiddenOperationException):
     *       - ADMIN: cualquier transición válida.
     *       - TECNICO: ASIGNADO -> EN_PROGRESO y EN_PROGRESO -> RESUELTO.
     *       - USUARIO: ABIERTO -> CANCELADO, RESUELTO -> CERRADO, RESUELTO -> EN_PROGRESO (reabrir).
     *  4. Efectos:
     *       - a EN_PROGRESO por primera vez: firstResponseAt = now (si ya tenía valor, no lo pises).
     *       - a RESUELTO: resolvedAt = now.
     *       - reapertura (RESUELTO -> EN_PROGRESO): resolvedAt = null.
     *  5. Guarda el estado anterior, asigna status = target y devuelve el anterior.
     */
    public TicketStatus changeStatus(TicketStatus target, Role actorRole, Instant now) {
        throw new UnsupportedOperationException("TODO: Ticket.changeStatus");
    }

    /** EJEMPLO RESUELTO: vencido = sigue activo y ya pasó la fecha límite. Úsalo como guía para isNearSlaBreach. */
    public boolean isOverdue(Instant now) {
        return status.isActive() && now.isAfter(slaDeadline);
    }

    /**
     * ¿Está próximo a incumplir el SLA?
     *
     * TODO (tú): implementar.
     *  1. Si !status.isActive() -> false.
     *  2. total = Duration.between(createdAt, slaDeadline); remaining = Duration.between(now, slaDeadline).
     *  3. Devuelve true si remaining <= total * thresholdPercent / 100
     *     (incluye los ya vencidos, cuyo remaining es negativo).
     *  Pista: trabaja en segundos o milisegundos (toMillis()) para no perder precisión.
     */
    public boolean isNearSlaBreach(Instant now, int thresholdPercent) {
        throw new UnsupportedOperationException("TODO: Ticket.isNearSlaBreach");
    }

    /**
     * Escalamiento automático. Devuelve la prioridad anterior (para el historial).
     *
     * TODO (tú): implementar.
     *  1. Si ya está escalado -> BusinessRuleException("El ticket ya fue escalado").
     *  2. Si !status.isActive() -> BusinessRuleException.
     *  3. previous = priority; priority = priority.next(); escalated = true; devuelve previous.
     *  Decisión: la fecha límite de SLA NO cambia (el compromiso con el usuario se mantiene).
     */
    public Priority escalate() {
        throw new UnsupportedOperationException("TODO: Ticket.escalate");
    }

    public boolean isOwnedBy(Long userId) {
        return requesterId != null && requesterId.equals(userId);
    }

    public boolean isAssignedTo(Long technicianId) {
        return this.technicianId != null && this.technicianId.equals(technicianId);
    }

    /** Tiempo desde la creación hasta un instante (para las métricas de primera respuesta y resolución). */
    public Duration elapsedUntil(Instant instant) {
        return Duration.between(createdAt, instant);
    }

    public Long getId() {
        return id;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public Priority getPriority() {
        return priority;
    }

    public String getSede() {
        return sede;
    }

    public String getAffectedAsset() {
        return affectedAsset;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public Long getTechnicianId() {
        return technicianId;
    }

    public boolean isEscalated() {
        return escalated;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getSlaDeadline() {
        return slaDeadline;
    }

    public Instant getFirstResponseAt() {
        return firstResponseAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public Long getVersion() {
        return version;
    }
}
