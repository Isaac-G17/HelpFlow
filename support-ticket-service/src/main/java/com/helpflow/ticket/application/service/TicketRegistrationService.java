package com.helpflow.ticket.application.service;

import com.helpflow.ticket.application.port.in.CreateTicketUseCase.CreateTicketCommand;
import com.helpflow.ticket.application.port.out.SlaPolicyRepositoryPort;
import com.helpflow.ticket.application.port.out.TicketHistoryRepositoryPort;
import com.helpflow.ticket.application.port.out.TicketMetricsPort;
import com.helpflow.ticket.application.port.out.TicketRepositoryPort;
import com.helpflow.ticket.application.port.out.UserRepositoryPort;
import com.helpflow.ticket.domain.exception.ResourceNotFoundException;
import com.helpflow.ticket.domain.model.CurrentUser;
import com.helpflow.ticket.domain.model.Ticket;
import com.helpflow.ticket.domain.model.TicketHistory;
import com.helpflow.ticket.domain.model.enums.HistoryAction;
import com.helpflow.ticket.domain.model.enums.TicketStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

/**
 * Pasos transaccionales del registro de un ticket. Está en un bean aparte de CreateTicketService
 * porque @Transactional funciona por proxy: si CreateTicketService llamara a un método @Transactional
 * de sí mismo, la anotación se ignoraría (auto-invocación).
 */
@Service
public class TicketRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(TicketRegistrationService.class);

    private final TicketRepositoryPort ticketRepository;
    private final TicketHistoryRepositoryPort historyRepository;
    private final UserRepositoryPort userRepository;
    private final SlaPolicyRepositoryPort slaPolicyRepository;
    private final TicketMetricsPort metrics;
    private final Clock clock;

    public TicketRegistrationService(TicketRepositoryPort ticketRepository, TicketHistoryRepositoryPort historyRepository,
                                     UserRepositoryPort userRepository, SlaPolicyRepositoryPort slaPolicyRepository,
                                     TicketMetricsPort metrics, Clock clock) {
        this.ticketRepository = ticketRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.slaPolicyRepository = slaPolicyRepository;
        this.metrics = metrics;
        this.clock = clock;
    }

    /**
     * Guarda el ticket ABIERTO + historial CREATED en una transacción.
     *
     * TODO (tú): implementar.
     *  1. now = clock.instant().
     *  2. requester = userRepository.findById(actor.id()) o ResourceNotFoundException("Usuario", id).
     *  3. policy = slaPolicyRepository.findByPriority(command.priority()) o ResourceNotFoundException.
     *  4. ticket = Ticket.open(requester, ...campos del command..., policy, now).
     *  5. saved = ticketRepository.save(ticket)   (ahora tiene id).
     *  6. historyRepository.save(TicketHistory.of(saved.getId(), null, ABIERTO, CREATED,
     *        "Ticket registrado", actor.id(), now)).
     *  7. metrics.ticketCreated(category, priority); log.info(...) con id, categoría, prioridad y slaDeadline.
     *  8. return saved.
     */
    @Transactional
    public Ticket open(CreateTicketCommand command, CurrentUser actor) {
        throw new UnsupportedOperationException("TODO: TicketRegistrationService.open");
    }

    /** Deja constancia de que dispatch no respondió: el ticket queda ABIERTO y no se pierde. */
    @Transactional
    public void recordDispatchFailure(Long ticketId, String reason) {
        historyRepository.save(TicketHistory.of(ticketId, TicketStatus.ABIERTO, TicketStatus.ABIERTO,
                HistoryAction.DISPATCH_FAILED, "Dispatch no disponible: " + reason, null, clock.instant()));
        log.warn("Ticket {} queda ABIERTO: technician-dispatch-service no disponible ({})", ticketId, reason);
    }

    @Transactional(readOnly = true)
    public Ticket reload(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));
    }
}
