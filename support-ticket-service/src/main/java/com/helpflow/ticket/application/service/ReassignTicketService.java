package com.helpflow.ticket.application.service;

import com.helpflow.ticket.application.port.in.ReassignTicketUseCase;
import com.helpflow.ticket.application.port.out.AssignmentRepositoryPort;
import com.helpflow.ticket.application.port.out.TechnicianRepositoryPort;
import com.helpflow.ticket.application.port.out.TicketHistoryRepositoryPort;
import com.helpflow.ticket.application.port.out.TicketMetricsPort;
import com.helpflow.ticket.application.port.out.TicketRepositoryPort;
import com.helpflow.ticket.domain.exception.ResourceNotFoundException;
import com.helpflow.ticket.domain.model.Assignment;
import com.helpflow.ticket.domain.model.CurrentUser;
import com.helpflow.ticket.domain.model.Technician;
import com.helpflow.ticket.domain.model.TicketHistory;
import com.helpflow.ticket.domain.model.Ticket;
import com.helpflow.ticket.domain.model.enums.AssignmentOrigin;
import com.helpflow.ticket.domain.model.enums.HistoryAction;
import com.helpflow.ticket.domain.model.enums.TicketStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;

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
    private static final Logger log = LoggerFactory.getLogger(ReassignTicketService.class);

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
        Instant now = clock.instant();
        Ticket ticket = ticketRepository.findById(command.ticketId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", command.ticketId()));
        Technician newTechnician = technicianRepository.findById(command.technicianId())
                .orElseThrow(() -> new ResourceNotFoundException("Técnico", command.technicianId()));
        newTechnician.ensureActive();

        TicketStatus previousStatus = ticket.getStatus();
        if (previousStatus == TicketStatus.ABIERTO) {
            ticket.assignTo(newTechnician);
        } else {
            Technician previous = technicianRepository.findById(ticket.getTechnicianId())
                    .orElseThrow(() -> new ResourceNotFoundException("Técnico", ticket.getTechnicianId()));
            ticket.reassignTo(previous, newTechnician);
            technicianRepository.save(previous);
        }
        technicianRepository.save(newTechnician);
        Ticket saved = ticketRepository.save(ticket);

        assignmentRepository.findActiveByTicketId(saved.getId()).ifPresent(active -> {
            active.deactivate();
            assignmentRepository.save(active);
        });
        assignmentRepository.save(Assignment.manual(saved.getId(), newTechnician.getId(), now));

        historyRepository.save(TicketHistory.of(saved.getId(), previousStatus, TicketStatus.ASIGNADO,
                HistoryAction.REASSIGNED, "Reasignado a " + newTechnician.getCode() + ": " + command.observation(),
                actor.id(), now));
        metrics.ticketAssigned(AssignmentOrigin.MANUAL);
        log.info("Ticket {} reasignado a {} por {}", saved.getId(), newTechnician.getCode(), actor.email());
        return saved;
    }
}
