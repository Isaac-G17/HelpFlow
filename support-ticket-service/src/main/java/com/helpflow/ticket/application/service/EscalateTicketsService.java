package com.helpflow.ticket.application.service;

import com.helpflow.ticket.application.port.in.EscalateTicketsUseCase;
import com.helpflow.ticket.application.port.out.SlaSettingsPort;
import com.helpflow.ticket.application.port.out.TicketHistoryRepositoryPort;
import com.helpflow.ticket.application.port.out.TicketMetricsPort;
import com.helpflow.ticket.application.port.out.TicketRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

/** Escalamiento por SLA. Lo dispara el scheduler (cada 5 min) o un ADMIN a mano. */
@Service
public class EscalateTicketsService implements EscalateTicketsUseCase {

    private final TicketRepositoryPort ticketRepository;
    private final TicketHistoryRepositoryPort historyRepository;
    private final SlaSettingsPort slaSettings;
    private final TicketMetricsPort metrics;
    private final Clock clock;

    public EscalateTicketsService(TicketRepositoryPort ticketRepository, TicketHistoryRepositoryPort historyRepository,
                                  SlaSettingsPort slaSettings, TicketMetricsPort metrics, Clock clock) {
        this.ticketRepository = ticketRepository;
        this.historyRepository = historyRepository;
        this.slaSettings = slaSettings;
        this.metrics = metrics;
        this.clock = clock;
    }

    /**
     * TODO (tú): implementar.
     *  1. now = clock.instant(); threshold = slaSettings.escalationThresholdPercent().
     *  2. Recorre ticketRepository.findActiveNotEscalated():
     *       si ticket.isNearSlaBreach(now, threshold):
     *          previous = ticket.escalate();
     *          ticketRepository.save(ticket);
     *          historyRepository.save(TicketHistory.of(id, status, status, ESCALATED,
     *              "Escalado por SLA: " + previous + " -> " + ticket.getPriority(), null, now));
     *          contador++
     *  3. metrics.ticketsEscalated(contador); log.info("Escalamiento SLA: {} tickets", contador).
     *  4. return contador.
     *  Pregunta de defensa: ¿qué pasa con la transacción si falla el ticket número 3 de 5?
     */
    @Override
    @Transactional
    public int escalateNearBreach() {
        throw new UnsupportedOperationException("TODO: EscalateTicketsService.escalateNearBreach");
    }
}
