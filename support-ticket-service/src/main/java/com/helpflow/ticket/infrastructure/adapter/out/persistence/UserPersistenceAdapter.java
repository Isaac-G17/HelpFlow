package com.helpflow.ticket.infrastructure.adapter.out.persistence;

import com.helpflow.ticket.application.port.out.UserRepositoryPort;
import com.helpflow.ticket.domain.model.PageQuery;
import com.helpflow.ticket.domain.model.PageResult;
import com.helpflow.ticket.domain.model.User;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.mapper.PersistenceMapper;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserJpaRepository repository;
    private final PersistenceMapper mapper;

    @Override
    public User save(User user) {
        return mapper.toDomain(repository.save(mapper.toEntity(user)));
    }

    @Override
    public Optional<User> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public boolean existsByDocument(String document) {
        return repository.existsByDocument(document);
    }

    @Override
    public PageResult<User> findAll(PageQuery pageQuery) {
        Page<UserEntity> page = repository.findAll(
                PageRequest.of(pageQuery.page(), pageQuery.size(), Sort.by("id")));
        return new PageResult<>(page.map(mapper::toDomain).getContent(), pageQuery.page(), pageQuery.size(),
                page.getTotalElements());
    }
}
