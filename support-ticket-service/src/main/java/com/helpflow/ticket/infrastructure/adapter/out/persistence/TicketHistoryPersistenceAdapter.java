package com.helpflow.ticket.infrastructure.adapter.out.persistence;

import com.helpflow.ticket.application.port.out.TicketHistoryRepositoryPort;
import com.helpflow.ticket.domain.model.TicketHistory;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.entity.TicketHistoryEntity;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.mapper.PersistenceMapper;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.repository.TicketHistoryJpaRepository;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.repository.TicketJpaRepository;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TicketHistoryPersistenceAdapter implements TicketHistoryRepositoryPort {

    private final TicketHistoryJpaRepository repository;
    private final TicketJpaRepository ticketRepository;
    private final UserJpaRepository userRepository;
    private final PersistenceMapper mapper;

    @Override
    public TicketHistory save(TicketHistory history) {
        TicketHistoryEntity entity = mapper.toEntity(history);
        entity.setTicket(ticketRepository.getReferenceById(history.ticketId()));
        if (history.performedBy() != null) {
            entity.setPerformedBy(userRepository.getReferenceById(history.performedBy()));
        }
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public List<TicketHistory> findByTicketId(Long ticketId) {
        return repository.findByTicket_IdOrderByCreatedAtAscIdAsc(ticketId).stream().map(mapper::toDomain).toList();
    }
}
