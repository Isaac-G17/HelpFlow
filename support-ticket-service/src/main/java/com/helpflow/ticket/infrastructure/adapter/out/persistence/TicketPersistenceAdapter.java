package com.helpflow.ticket.infrastructure.adapter.out.persistence;

import com.helpflow.ticket.application.port.out.TicketRepositoryPort;
import com.helpflow.ticket.domain.exception.ResourceNotFoundException;
import com.helpflow.ticket.domain.model.PageQuery;
import com.helpflow.ticket.domain.model.PageResult;
import com.helpflow.ticket.domain.model.Ticket;
import com.helpflow.ticket.domain.model.TicketFilter;
import com.helpflow.ticket.domain.model.TicketSummary;
import com.helpflow.ticket.domain.model.enums.TicketStatus;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.entity.TicketEntity;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.mapper.PersistenceMapper;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.repository.TechnicianJpaRepository;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.repository.TicketJpaRepository;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.repository.TicketSpecifications;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TicketPersistenceAdapter implements TicketRepositoryPort {

    private final TicketJpaRepository repository;
    private final UserJpaRepository userRepository;
    private final TechnicianJpaRepository technicianRepository;
    private final PersistenceMapper mapper;

    @Override
    public Ticket save(Ticket ticket) {
        TicketEntity entity;
        if (ticket.getId() == null) {
            entity = mapper.toEntity(ticket);
            entity.setRequester(userRepository.getReferenceById(ticket.getRequesterId()));
        } else {
            entity = repository.findById(ticket.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticket.getId()));
            OptimisticLock.check(TicketEntity.class, entity.getId(), ticket.getVersion(), entity.getVersion());
            mapper.updateEntity(ticket, entity);
        }
        // getReferenceById: asigna la FK sin hacer SELECT del técnico.
        entity.setTechnician(ticket.getTechnicianId() == null
                ? null
                : technicianRepository.getReferenceById(ticket.getTechnicianId()));
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<Ticket> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public PageResult<TicketSummary> findAll(TicketFilter filter, PageQuery pageQuery) {
        Page<TicketEntity> page = repository.findAll(TicketSpecifications.from(filter),
                PageRequest.of(pageQuery.page(), pageQuery.size(), Sort.by(Sort.Direction.DESC, "createdAt")));
        return new PageResult<>(page.getContent().stream().map(mapper::toSummary).toList(),
                pageQuery.page(), pageQuery.size(), page.getTotalElements());
    }

    @Override
    public List<Ticket> findActiveNotEscalated() {
        return repository.findByStatusInAndEscalatedFalse(TicketStatus.activeStatuses()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countOverdue(Instant now) {
        return repository.countByStatusInAndSlaDeadlineBefore(TicketStatus.activeStatuses(), now);
    }
}
