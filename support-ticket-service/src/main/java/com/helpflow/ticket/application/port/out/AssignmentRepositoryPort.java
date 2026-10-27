package com.helpflow.ticket.application.port.out;

import com.helpflow.ticket.domain.model.Assignment;

import java.util.Optional;

public interface AssignmentRepositoryPort {

    Assignment save(Assignment assignment);

    Optional<Assignment> findActiveByTicketId(Long ticketId);
}
