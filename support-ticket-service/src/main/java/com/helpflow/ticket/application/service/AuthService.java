package com.helpflow.ticket.application.service;

import com.helpflow.ticket.application.port.in.LoginUseCase;
import com.helpflow.ticket.application.port.in.RegisterUserUseCase;
import com.helpflow.ticket.application.port.out.PasswordEncoderPort;
import com.helpflow.ticket.application.port.out.TokenProviderPort;
import com.helpflow.ticket.application.port.out.UserRepositoryPort;
import com.helpflow.ticket.domain.exception.DuplicateResourceException;
import com.helpflow.ticket.domain.exception.InvalidCredentialsException;
import com.helpflow.ticket.domain.model.User;
import com.helpflow.ticket.domain.model.enums.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Locale;

/** EJEMPLO RESUELTO de caso de uso: valida, delega en el dominio y persiste por puertos. */
@Service
public class AuthService implements RegisterUserUseCase, LoginUseCase {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenProviderPort tokenProvider;
    private final Clock clock;

    public AuthService(UserRepositoryPort userRepository, PasswordEncoderPort passwordEncoder,
                       TokenProviderPort tokenProvider, Clock clock) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.clock = clock;
    }

    @Override
    @Transactional
    public User register(RegisterCommand command) {
        String email = command.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Ya existe un usuario con el correo " + email);
        }
        if (userRepository.existsByDocument(command.document().trim())) {
            throw new DuplicateResourceException("Ya existe un usuario con el documento " + command.document());
        }
        User user = User.create(command.document(), command.name(), email, command.sede(), command.area(),
                passwordEncoder.encode(command.password()), Role.USUARIO, clock.instant());
        User saved = userRepository.save(user);
        log.info("Usuario registrado id={} email={} role={}", saved.getId(), saved.getEmail(), saved.getRole());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResult login(LoginCommand command) {
        String email = command.email().trim().toLowerCase(Locale.ROOT);
        // Mismo error si el correo no existe, la contraseña no coincide o el usuario está inactivo:
        // así no se revela qué correos están registrados.
        User user = userRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(command.password(), u.getPasswordHash()))
                .filter(User::isActive)
                .orElseThrow(InvalidCredentialsException::new);
        log.info("Login correcto id={} role={}", user.getId(), user.getRole());
        return new LoginResult(tokenProvider.generateToken(user), tokenProvider.expirationSeconds(), user.getRole());
    }
}
