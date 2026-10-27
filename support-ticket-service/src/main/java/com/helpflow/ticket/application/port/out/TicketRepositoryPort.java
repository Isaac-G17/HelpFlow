package com.helpflow.ticket.application.port.out;

import com.helpflow.ticket.domain.model.PageQuery;
import com.helpflow.ticket.domain.model.PageResult;
import com.helpflow.ticket.domain.model.Ticket;
import com.helpflow.ticket.domain.model.TicketFilter;
import com.helpflow.ticket.domain.model.TicketSummary;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TicketRepositoryPort {

    Ticket save(Ticket ticket);

    Optional<Ticket> findById(Long id);

    PageResult<TicketSummary> findAll(TicketFilter filter, PageQuery pageQuery);

    /** Tickets ABIERTO/ASIGNADO/EN_PROGRESO que aún no han sido escalados. */
    List<Ticket> findActiveNotEscalated();

    /** Tickets activos cuya fecha límite ya pasó (alimenta el gauge helpflow.tickets.overdue). */
    long countOverdue(Instant now);
}
