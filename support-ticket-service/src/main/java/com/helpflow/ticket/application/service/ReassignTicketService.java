package com.helpflow.ticket.application.service;

import com.helpflow.ticket.application.port.in.ReassignTicketUseCase;
import com.helpflow.ticket.application.port.out.AssignmentRepositoryPort;
import com.helpflow.ticket.application.port.out.TechnicianRepositoryPort;
import com.helpflow.ticket.application.port.out.TicketHistoryRepositoryPort;
import com.helpflow.ticket.application.port.out.TicketMetricsPort;
import com.helpflow.ticket.application.port.out.TicketRepositoryPort;
import com.helpflow.ticket.domain.model.CurrentUser;
import com.helpflow.ticket.domain.model.Ticket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

/** Reasignación manual (solo ADMIN, lo exige el controller con @PreAuthorize). Transaccional. */
@Service
public class ReassignTicketService implements ReassignTicketUseCase {

    private final TicketRepositoryPort ticketRepository;
    private final TechnicianRepositoryPort technicianRepository;
    private final AssignmentRepositoryPort assignmentRepository;
    private final TicketHistoryRepositoryPort historyRepository;
    private final TicketMetricsPort metrics;
    private final Clock clock;

    public ReassignTicketService(TicketRepositoryPort ticketRepository, TechnicianRepositoryPort technicianRepository,
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
     *  1. now; ticket (404 si no existe); newTech = technicianRepository.findById(command.technicianId()) (404).
     *  2. newTech.ensureActive().
     *  3. Si el ticket está ABIERTO (sin técnico): usa ticket.assignTo(newTech) en lugar de reassignTo.
     *     Si no: previous = technicianRepository.findById(ticket.getTechnicianId()) y
     *            ticket.reassignTo(previous, newTech); technicianRepository.save(previous).
     *  4. technicianRepository.save(newTech); saved = ticketRepository.save(ticket).
     *  5. Desactiva la asignación activa anterior (assignmentRepository.findActiveByTicketId -> deactivate -> save)
     *     y guarda Assignment.manual(...). Ojo al ORDEN: el índice único solo permite UNA activa por ticket.
     *  6. Historial REASSIGNED con la observación del ADMIN; metrics.ticketAssigned(MANUAL); log.info.
     */
    @Override
    @Transactional
    public Ticket reassign(ReassignTicketCommand command, CurrentUser actor) {
        throw new UnsupportedOperationException("TODO: ReassignTicketService.reassign");
    }
}
