package com.helpflow.ticket.infrastructure.adapter.out.persistence.repository;

import com.helpflow.ticket.infrastructure.adapter.out.persistence.entity.TicketHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketHistoryJpaRepository extends JpaRepository<TicketHistoryEntity, Long> {

    List<TicketHistoryEntity> findByTicket_IdOrderByCreatedAtAscIdAsc(Long ticketId);
}
