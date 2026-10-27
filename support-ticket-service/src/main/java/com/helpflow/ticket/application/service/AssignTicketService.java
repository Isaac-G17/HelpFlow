package com.helpflow.ticket.application.service;

import com.helpflow.ticket.application.port.in.AssignTicketUseCase;
import com.helpflow.ticket.application.port.out.AssignmentRepositoryPort;
import com.helpflow.ticket.application.port.out.TechnicianRepositoryPort;
import com.helpflow.ticket.application.port.out.TicketHistoryRepositoryPort;
import com.helpflow.ticket.application.port.out.TicketMetricsPort;
import com.helpflow.ticket.application.port.out.TicketRepositoryPort;
import com.helpflow.ticket.domain.model.AssignmentOrigin;
import com.helpflow.ticket.domain.model.DispatchSuggestion;
import com.helpflow.ticket.domain.model.Priority;
import com.helpflow.ticket.domain.model.Technician;
import com.helpflow.ticket.domain.model.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * OPERACIÓN CRÍTICA. Todo ocurre en UNA transacción: si algo falla (p. ej. bloqueo optimista
 * del técnico), no queda ni la asignación, ni el historial, ni la carga a medias.
 */
@Service
public class AssignTicketService implements AssignTicketUseCase {

    private static final Logger log = LoggerFactory.getLogger(AssignTicketService.class);

    private final TicketRepositoryPort ticketRepository;
    private final TechnicianRepositoryPort technicianRepository;
    private final AssignmentRepositoryPort assignmentRepository;
    private final TicketHistoryRepositoryPort historyRepository;
    private final TicketMetricsPort metrics;
    private final Clock clock;

    public AssignTicketService(TicketRepositoryPort ticketRepository, TechnicianRepositoryPort technicianRepository,
                               AssignmentRepositoryPort assignmentRepository,
                               TicketHistoryRepositoryPort historyRepository, TicketMetricsPort metrics, Clock clock) {
        this.ticketRepository = ticketRepository;
        this.technicianRepository = technicianRepository;
        this.assignmentRepository = assignmentRepository;
        this.historyRepository = historyRepository;
        this.metrics = metrics;
        this.clock = clock;
    }

    /**
     * TODO (tú): implementar.
     *  1. now = clock.instant(); ticket = ticketRepository.findById(...) o ResourceNotFoundException.
     *  2. Optional<Selection> selection = selectTechnician(ticket, command.suggestion(), now);
     *  3. Si está vacío (nadie cumple):
     *       - historial: TicketHistory.of(id, ABIERTO, ABIERTO, DISPATCH_FAILED,
     *             "Sin técnico disponible que cumpla las reglas de asignación", performedBy, now)
     *       - log.warn y return ticket (queda ABIERTO).
     *  4. Si hay técnico:
     *       - ticket.assignTo(technician)            (dominio: valida y sube la carga)
     *       - technicianRepository.save(technician)  (persiste la carga; @Version detecta concurrencia)
     *       - saved = ticketRepository.save(ticket)
     *       - assignmentRepository.save(origin == DISPATCH
     *             ? Assignment.fromDispatch(id, techId, suggestion, now)
     *             : Assignment.fallback(id, techId, now))
     *       - historyRepository.save(TicketHistory.of(id, ABIERTO, ASIGNADO, ASSIGNED,
     *             "Asignado a " + code + " (" + origin + ")", performedBy, now))
     *       - metrics.ticketAssigned(origin); log.info(...)
     *       - return saved.
     */
    @Override
    @Transactional
    public Ticket assign(AssignTicketCommand command) {
        throw new UnsupportedOperationException("TODO: AssignTicketService.assign");
    }

    /**
     * Elige el técnico: primero el sugerido por dispatch; si no cumple, uno local (fallback).
     *
     * TODO (tú): implementar.
     *  1. Busca el sugerido: technicianRepository.findByCode(suggestion.technicianCode()).
     *  2. Si existe y isEligible(...) -> Optional.of(new Selection(tech, AssignmentOrigin.DISPATCH)).
     *  3. Si no, recorre technicianRepository.findActiveCandidates(ticket.getCategory())
     *     (ya vienen ordenados por menor carga) y elige el primero que:
     *       - tenga capacidad (hasCapacity)
     *       - preferiblemente sea de la misma sede (o cualquier sede si la prioridad es CRITICA).
     *     Pista: primero intenta misma sede; si no hay, y es CRITICA, acepta otra sede.
     *     -> Optional.of(new Selection(tech, AssignmentOrigin.FALLBACK)).
     *  4. Si nadie cumple -> Optional.empty().
     */
    Optional<Selection> selectTechnician(Ticket ticket, DispatchSuggestion suggestion, Instant now) {
        throw new UnsupportedOperationException("TODO: AssignTicketService.selectTechnician");
    }

    /**
     * Reglas internas de asignación del enunciado (las 4 deben cumplirse).
     *
     * TODO (tú): implementar y devolver true solo si TODAS se cumplen:
     *  1. Técnico activo:            technician.isActive()
     *  2. Categoría/especialidad:    technician.canAttend(ticket.getCategory())
     *  3. Carga máxima:              technician.hasCapacity()
     *  4. Sede:                      misma sede que el ticket, salvo prioridad CRITICA (cualquier sede).
     *  5. Cumplimiento del SLA:      now + suggestion.estimatedHours() horas <= ticket.getSlaDeadline()
     */
    boolean isEligible(Technician technician, Ticket ticket, DispatchSuggestion suggestion, Instant now) {
        throw new UnsupportedOperationException("TODO: AssignTicketService.isEligible");
    }

    /** Resultado de la selección: qué técnico y por qué vía. */
    record Selection(Technician technician, AssignmentOrigin origin) {
    }

    /** Ayuda para la regla de sede (ya implementada). */
    static boolean sedeAllowed(Technician technician, Ticket ticket) {
        return ticket.getPriority() == Priority.CRITICA || technician.getSede().equals(ticket.getSede());
    }
}
