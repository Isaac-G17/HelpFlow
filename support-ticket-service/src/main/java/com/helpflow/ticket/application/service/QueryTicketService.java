package com.helpflow.ticket.application.service;

import com.helpflow.ticket.application.port.in.QueryTicketUseCase;
import com.helpflow.ticket.application.port.out.TechnicianRepositoryPort;
import com.helpflow.ticket.application.port.out.TicketHistoryRepositoryPort;
import com.helpflow.ticket.application.port.out.TicketRepositoryPort;
import com.helpflow.ticket.domain.exception.ResourceNotFoundException;
import com.helpflow.ticket.domain.exception.ForbiddenOperationException;
import com.helpflow.ticket.domain.model.CurrentUser;
import com.helpflow.ticket.domain.model.PageQuery;
import com.helpflow.ticket.domain.model.PageResult;
import com.helpflow.ticket.domain.model.Technician;
import com.helpflow.ticket.domain.model.Ticket;
import com.helpflow.ticket.domain.model.TicketFilter;
import com.helpflow.ticket.domain.model.TicketHistory;
import com.helpflow.ticket.domain.model.TicketSummary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class QueryTicketService implements QueryTicketUseCase {

    private final TicketRepositoryPort ticketRepository;
    private final TicketHistoryRepositoryPort historyRepository;
    private final TechnicianRepositoryPort technicianRepository;

    public QueryTicketService(TicketRepositoryPort ticketRepository, TicketHistoryRepositoryPort historyRepository,
                              TechnicianRepositoryPort technicianRepository) {
        this.ticketRepository = ticketRepository;
        this.historyRepository = historyRepository;
        this.technicianRepository = technicianRepository;
    }

    @Override
    public Ticket getById(Long ticketId, CurrentUser actor) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));
        ensureCanView(ticket, actor);
        return ticket;
    }

    /**
     * EJEMPLO RESUELTO de propiedad en el caso de uso: el filtro se fuerza según el rol,
     * así un USUARIO nunca puede pedir tickets ajenos aunque mande otros parámetros.
     */
    @Override
    public PageResult<TicketSummary> list(TicketFilter filter, PageQuery pageQuery, CurrentUser actor) {
        TicketFilter effective = switch (actor.role()) {
            case ADMIN -> filter;
            case USUARIO -> filter.withRequester(actor.id());
            case TECNICO -> {
                Optional<Technician> technician = technicianRepository.findByUserId(actor.id());
                if (technician.isEmpty()) {
                    yield null;
                }
                yield filter.withTechnician(technician.get().getId());
            }
        };
        if (effective == null) {
            return new PageResult<>(List.of(), pageQuery.page(), pageQuery.size(), 0);
        }
        return ticketRepository.findAll(effective, pageQuery);
    }

    @Override
    public List<TicketHistory> history(Long ticketId, CurrentUser actor) {
        getById(ticketId, actor); // 404 si no existe, 403 si no es suyo
        return historyRepository.findByTicketId(ticketId);
    }

    /**
     * Regla de propiedad: ¿puede este actor ver el ticket?
     *
     * TODO (tú): implementar.
     *  1. ADMIN -> puede ver todo (return).
     *  2. USUARIO -> solo si ticket.isOwnedBy(actor.id()); si no -> ForbiddenOperationException.
     *  3. TECNICO -> busca su perfil con technicianRepository.findByUserId(actor.id()) y
     *     solo si ticket.isAssignedTo(technician.getId()); si no -> ForbiddenOperationException.
     *  Usa un mensaje genérico, p. ej. "No tiene acceso a este ticket".
     *  Este mismo método lo reutilizarás en ChangeTicketStatusService (o extráelo a una clase TicketAccessPolicy).
     */
    void ensureCanView(Ticket ticket, CurrentUser actor) {
        boolean allowed = switch (actor.role()) {
            case ADMIN -> true;
            case USUARIO -> ticket.isOwnedBy(actor.id());
            case TECNICO -> technicianRepository.findByUserId(actor.id())
                    .map(technician -> ticket.isAssignedTo(technician.getId()))
                    .orElse(false);
        };
        if (!allowed) {
            throw new ForbiddenOperationException("No tiene acceso a este ticket");
        }
    }
}
