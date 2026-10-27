package com.helpflow.ticket.application.port.out;

import com.helpflow.ticket.domain.model.PageQuery;
import com.helpflow.ticket.domain.model.PageResult;
import com.helpflow.ticket.domain.model.User;

import java.util.Optional;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByDocument(String document);

    PageResult<User> findAll(PageQuery pageQuery);
}
