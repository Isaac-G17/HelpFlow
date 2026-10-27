package com.helpflow.ticket.infrastructure.adapter.out.persistence;

import com.helpflow.ticket.application.port.out.SlaPolicyRepositoryPort;
import com.helpflow.ticket.domain.model.Priority;
import com.helpflow.ticket.domain.model.SlaPolicy;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.entity.SlaPolicyEntity;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.mapper.PersistenceMapper;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.repository.SlaPolicyJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SlaPolicyPersistenceAdapter implements SlaPolicyRepositoryPort {

    private final SlaPolicyJpaRepository repository;
    private final PersistenceMapper mapper;
    private final Clock clock;

    @Override
    public Optional<SlaPolicy> findByPriority(Priority priority) {
        return repository.findById(priority).map(mapper::toDomain);
    }

    @Override
    public List<SlaPolicy> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .sorted(Comparator.comparing(SlaPolicy::priority))
                .toList();
    }

    @Override
    public SlaPolicy save(SlaPolicy policy) {
        SlaPolicyEntity entity = repository.findById(policy.priority()).orElseGet(() -> {
            SlaPolicyEntity created = new SlaPolicyEntity();
            created.setPriority(policy.priority());
            return created;
        });
        entity.setResponseHours(policy.responseHours());
        entity.setResolutionHours(policy.resolutionHours());
        entity.setUpdatedAt(clock.instant());
        return mapper.toDomain(repository.save(entity));
    }
}
