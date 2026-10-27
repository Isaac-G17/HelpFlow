package com.helpflow.ticket.application.service;

import com.helpflow.ticket.application.port.in.ManageUserUseCase;
import com.helpflow.ticket.application.port.out.UserRepositoryPort;
import com.helpflow.ticket.domain.exception.ResourceNotFoundException;
import com.helpflow.ticket.domain.model.AccountStatus;
import com.helpflow.ticket.domain.model.PageQuery;
import com.helpflow.ticket.domain.model.PageResult;
import com.helpflow.ticket.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService implements ManageUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepositoryPort userRepository;

    public UserService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public User getProfile(Long userId) {
        return load(userId);
    }

    @Override
    @Transactional
    public User updateProfile(Long userId, UpdateProfileCommand command) {
        User user = load(userId);
        user.updateProfile(command.name(), command.sede(), command.area());
        log.info("Perfil actualizado userId={}", userId);
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<User> list(PageQuery pageQuery) {
        return userRepository.findAll(pageQuery);
    }

    @Override
    @Transactional
    public User changeStatus(Long userId, AccountStatus status) {
        User user = load(userId);
        user.changeStatus(status);
        log.info("Estado de usuario cambiado userId={} status={}", userId, status);
        return userRepository.save(user);
    }

    private User load(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId));
    }
}
