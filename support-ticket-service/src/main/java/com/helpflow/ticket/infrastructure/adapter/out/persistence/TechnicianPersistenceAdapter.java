package com.helpflow.ticket.infrastructure.adapter.out.persistence;

import com.helpflow.ticket.application.port.out.TechnicianRepositoryPort;
import com.helpflow.ticket.domain.exception.ResourceNotFoundException;
import com.helpflow.ticket.domain.model.Technician;
import com.helpflow.ticket.domain.model.enums.AccountStatus;
import com.helpflow.ticket.domain.model.enums.Category;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.entity.TechnicianEntity;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.mapper.PersistenceMapper;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.repository.TechnicianJpaRepository;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TechnicianPersistenceAdapter implements TechnicianRepositoryPort {

    private final TechnicianJpaRepository repository;
    private final UserJpaRepository userRepository;
    private final PersistenceMapper mapper;

    @Override
    public Technician save(Technician technician) {
        TechnicianEntity entity;
        if (technician.getId() == null) {
            entity = mapper.toEntity(technician);
            entity.setUser(userRepository.getReferenceById(technician.getUserId()));
        } else {
            entity = repository.findById(technician.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Técnico", technician.getId()));
            OptimisticLock.check(TechnicianEntity.class, entity.getId(), technician.getVersion(), entity.getVersion());
            mapper.updateEntity(technician, entity);
        }
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<Technician> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Technician> findByCode(String code) {
        return repository.findByCode(code).map(mapper::toDomain);
    }

    @Override
    public Optional<Technician> findByUserId(Long userId) {
        return repository.findByUser_Id(userId).map(mapper::toDomain);
    }

    @Override
    public List<Technician> findAll() {
        return repository.findAllByOrderByCodeAsc().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Technician> findActiveCandidates(Category category) {
        List<TechnicianEntity> candidates = category == Category.OTRO
                ? repository.findByStatusOrderByActiveTicketsAscIdAsc(AccountStatus.ACTIVO)
                : repository.findByStatusAndSpecialtyOrderByActiveTicketsAscIdAsc(AccountStatus.ACTIVO, category);
        return candidates.stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByCode(String code) {
        return repository.existsByCode(code);
    }

    @Override
    public long count() {
        return repository.count();
    }
}
