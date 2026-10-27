package com.helpflow.ticket.application.port.in;

import com.helpflow.ticket.domain.model.CurrentUser;
import com.helpflow.ticket.domain.model.PageQuery;
import com.helpflow.ticket.domain.model.PageResult;
import com.helpflow.ticket.domain.model.Ticket;
import com.helpflow.ticket.domain.model.TicketFilter;
import com.helpflow.ticket.domain.model.TicketHistory;
import com.helpflow.ticket.domain.model.TicketSummary;

import java.util.List;

public interface QueryTicketUseCase {

    Ticket getById(Long ticketId, CurrentUser actor);

    PageResult<TicketSummary> list(TicketFilter filter, PageQuery pageQuery, CurrentUser actor);

    List<TicketHistory> history(Long ticketId, CurrentUser actor);
}
