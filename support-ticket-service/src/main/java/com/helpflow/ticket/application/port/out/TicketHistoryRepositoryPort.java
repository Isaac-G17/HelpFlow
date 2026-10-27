package com.helpflow.ticket.application.port.out;

import com.helpflow.ticket.domain.model.TicketHistory;

import java.util.List;

public interface TicketHistoryRepositoryPort {

    TicketHistory save(TicketHistory history);

    List<TicketHistory> findByTicketId(Long ticketId);
}
