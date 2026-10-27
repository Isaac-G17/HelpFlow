package com.helpflow.ticket.application.service;

import com.helpflow.ticket.application.port.in.ChangeTicketStatusUseCase;
import com.helpflow.ticket.application.port.out.TechnicianRepositoryPort;
import com.helpflow.ticket.application.port.out.TicketHistoryRepositoryPort;
import com.helpflow.ticket.application.port.out.TicketMetricsPort;
import com.helpflow.ticket.application.port.out.TicketRepositoryPort;
import com.helpflow.ticket.domain.model.CurrentUser;
import com.helpflow.ticket.domain.model.Ticket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class ChangeTicketStatusService implements ChangeTicketStatusUseCase {

    private final TicketRepositoryPort ticketRepository;
    private final TechnicianRepositoryPort technicianRepository;
    private final TicketHistoryRepositoryPort historyRepository;
    private final QueryTicketService queryTicketService;
    private final TicketMetricsPort metrics;
    private final Clock clock;

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
        throw new UnsupportedOperationException("TODO: ChangeTicketStatusService.changeStatus");
    }
}
