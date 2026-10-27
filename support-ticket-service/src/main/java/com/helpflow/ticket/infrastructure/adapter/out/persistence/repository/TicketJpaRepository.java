package com.helpflow.ticket.infrastructure.adapter.out.persistence.repository;

import com.helpflow.ticket.domain.model.enums.TicketStatus;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.entity.TicketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface TicketJpaRepository extends JpaRepository<TicketEntity, Long>, JpaSpecificationExecutor<TicketEntity> {

    /**
     * Listado paginado: trae solicitante y técnico en la MISMA consulta (LEFT JOIN FETCH).
     * Sin el @EntityGraph, mostrar requester.name y technician.code dispararía 1 + 2N consultas (N+1).
     */
    @Override
    @EntityGraph(attributePaths = {"requester", "technician"})
    Page<TicketEntity> findAll(Specification<TicketEntity> spec, Pageable pageable);

    List<TicketEntity> findByStatusInAndEscalatedFalse(Collection<TicketStatus> statuses);

    long countByStatusInAndSlaDeadlineBefore(Collection<TicketStatus> statuses, Instant now);
}
