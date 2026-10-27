package com.helpflow.ticket.application.service;

import com.helpflow.ticket.application.port.in.ChangeTicketStatusUseCase;
import com.helpflow.ticket.application.port.out.TechnicianRepositoryPort;
import com.helpflow.ticket.application.port.out.TicketHistoryRepositoryPort;
import com.helpflow.ticket.application.port.out.TicketMetricsPort;
import com.helpflow.ticket.application.port.out.TicketRepositoryPort;
import com.helpflow.ticket.domain.exception.ResourceNotFoundException;
import com.helpflow.ticket.domain.model.Technician;
import com.helpflow.ticket.domain.model.TicketHistory;
import com.helpflow.ticket.domain.model.enums.HistoryAction;
import com.helpflow.ticket.domain.model.enums.TicketStatus;
import com.helpflow.ticket.domain.model.CurrentUser;
import com.helpflow.ticket.domain.model.Ticket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.time.Clock;

@Service
public class ChangeTicketStatusService implements ChangeTicketStatusUseCase {

    private final TicketRepositoryPort ticketRepository;
    private final TechnicianRepositoryPort technicianRepository;
    private final TicketHistoryRepositoryPort historyRepository;
    private final QueryTicketService queryTicketService;
    private final TicketMetricsPort metrics;
    private final Clock clock;
    private static final Logger log = LoggerFactory.getLogger(ChangeTicketStatusService.class);

    public ChangeTicketStatusService(TicketRepositoryPort ticketRepository, TechnicianRepositoryPort technicianRepository,
                                     TicketHistoryRepositoryPort historyRepository,
                                     QueryTicketService queryTicketService, TicketMetricsPort metrics, Clock clock) {
        this.ticketRepository = ticketRepository;
        this.technicianRepository = technicianRepository;
        this.historyRepository = historyRepository;
        this.queryTicketService = queryTicketService;
        this.metrics = metrics;
        this.clock = clock;
    }

    /**
     * TODO (tú): implementar.
     *  1. now; ticket = queryTicketService.getById(command.ticketId(), actor)   -> 404 / 403 (propiedad).
     *  2. previous = ticket.changeStatus(command.target(), actor.role(), now)  -> 409 / 403 (estado y rol).
     *  3. Efectos sobre la carga del técnico (si el ticket tiene técnico):
     *       - a RESUELTO o CANCELADO (desde ASIGNADO/EN_PROGRESO): technician.releaseTicket()
     *       - reapertura RESUELTO -> EN_PROGRESO: technician.takeTicket()
     *     y technicianRepository.save(technician).
     *  4. saved = ticketRepository.save(ticket).
     *  5. Historial STATUS_CHANGED (previous -> target) con command.observation() y actor.id().
     *  6. Métricas: metrics.statusChanged(target);
     *       - si pasó a EN_PROGRESO por primera vez: metrics.firstResponse(ticket.elapsedUntil(now))
     *       - si pasó a RESUELTO: metrics.resolution(ticket.elapsedUntil(now))
     *  7. log.info y return saved.
     */
    @Override
    @Transactional
    public Ticket changeStatus(ChangeStatusCommand command, CurrentUser actor) {
        Instant now = clock.instant();
        Ticket ticket = queryTicketService.getById(command.ticketId(), actor);
        boolean firstResponse = ticket.getFirstResponseAt() == null;
        TicketStatus target = command.target();

        TicketStatus previous = ticket.changeStatus(target, actor.role(), now);
        updateTechnicianLoad(ticket, previous, target);
        Ticket saved = ticketRepository.save(ticket);

        historyRepository.save(TicketHistory.of(saved.getId(), previous, target, HistoryAction.STATUS_CHANGED,
                command.observation(), actor.id(), now));

        metrics.statusChanged(target);
        if (target == TicketStatus.EN_PROGRESO && firstResponse) {
            metrics.firstResponse(ticket.elapsedUntil(now));
        }
        if (target == TicketStatus.RESUELTO) {
            metrics.resolution(ticket.elapsedUntil(now));
        }
        log.info("Ticket {}: {} -> {} por {} ({})", saved.getId(), previous, target, actor.email(), actor.role());
        return saved;
    }

    private void updateTechnicianLoad(Ticket ticket, TicketStatus previous, TicketStatus target) {
        if (ticket.getTechnicianId() == null) {
            return;
        }
        boolean releases = (target == TicketStatus.RESUELTO || target == TicketStatus.CANCELADO)
                && (previous == TicketStatus.ASIGNADO || previous == TicketStatus.EN_PROGRESO);
        boolean reopens = previous == TicketStatus.RESUELTO && target == TicketStatus.EN_PROGRESO;
        if (!releases && !reopens) {
            return;
        }
        Technician technician = technicianRepository.findById(ticket.getTechnicianId())
                .orElseThrow(() -> new ResourceNotFoundException("Técnico", ticket.getTechnicianId()));
        if (releases) {
            technician.releaseTicket();
        } else {
            technician.takeTicket();
        }
        technicianRepository.save(technician);
    }
}
