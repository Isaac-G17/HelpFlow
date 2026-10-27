package com.helpflow.ticket.infrastructure.adapter.out.persistence.repository;

import com.helpflow.ticket.infrastructure.adapter.out.persistence.entity.AssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssignmentJpaRepository extends JpaRepository<AssignmentEntity, Long> {

    Optional<AssignmentEntity> findByTicket_IdAndActiveTrue(Long ticketId);
}
