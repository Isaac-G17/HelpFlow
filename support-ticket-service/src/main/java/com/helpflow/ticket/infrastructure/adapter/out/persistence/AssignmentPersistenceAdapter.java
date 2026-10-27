package com.helpflow.ticket.infrastructure.adapter.out.persistence;

import com.helpflow.ticket.application.port.out.AssignmentRepositoryPort;
import com.helpflow.ticket.domain.exception.ResourceNotFoundException;
import com.helpflow.ticket.domain.model.Assignment;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.entity.AssignmentEntity;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.mapper.PersistenceMapper;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.repository.AssignmentJpaRepository;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.repository.TechnicianJpaRepository;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.repository.TicketJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AssignmentPersistenceAdapter implements AssignmentRepositoryPort {

    private final AssignmentJpaRepository repository;
    private final TicketJpaRepository ticketRepository;
    private final TechnicianJpaRepository technicianRepository;
    private final PersistenceMapper mapper;

    @Override
    public Assignment save(Assignment assignment) {
        if (assignment.getId() == null) {
            AssignmentEntity entity = mapper.toEntity(assignment);
            entity.setTicket(ticketRepository.getReferenceById(assignment.getTicketId()));
            entity.setTechnician(technicianRepository.getReferenceById(assignment.getTechnicianId()));
            return mapper.toDomain(repository.save(entity));
        }
        // Una asignación existente solo cambia "active". saveAndFlush: Hibernate ejecuta los INSERT antes
        // que los UPDATE al hacer flush; sin el flush inmediato, la nueva asignación activa chocaría con
        // el índice único (una sola activa por ticket) antes de desactivar la anterior.
        AssignmentEntity entity = repository.findById(assignment.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Asignación", assignment.getId()));
        entity.setActive(assignment.isActive());
        return mapper.toDomain(repository.saveAndFlush(entity));
    }

    @Override
    public Optional<Assignment> findActiveByTicketId(Long ticketId) {
        return repository.findByTicket_IdAndActiveTrue(ticketId).map(mapper::toDomain);
    }
}
