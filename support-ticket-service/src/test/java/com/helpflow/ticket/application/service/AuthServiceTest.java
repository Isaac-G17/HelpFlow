package com.helpflow.ticket.application.service;

import com.helpflow.ticket.application.port.in.LoginUseCase.LoginCommand;
import com.helpflow.ticket.application.port.in.LoginUseCase.LoginResult;
import com.helpflow.ticket.application.port.in.RegisterUserUseCase.RegisterCommand;
import com.helpflow.ticket.application.port.out.PasswordEncoderPort;
import com.helpflow.ticket.application.port.out.TokenProviderPort;
import com.helpflow.ticket.application.port.out.UserRepositoryPort;
import com.helpflow.ticket.domain.exception.DuplicateResourceException;
import com.helpflow.ticket.domain.exception.InvalidCredentialsException;
import com.helpflow.ticket.domain.model.TestData;
import com.helpflow.ticket.domain.model.User;
import com.helpflow.ticket.domain.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private PasswordEncoderPort passwordEncoder;
    @Mock
    private TokenProviderPort tokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, tokenProvider,
                Clock.fixed(TestData.NOW, ZoneOffset.UTC));
    }

    @Test
    void registraUsuarioConRolBasicoYContrasenaCifrada() {
        when(userRepository.existsByEmail("nuevo@helpflow.com")).thenReturn(false);
        when(userRepository.existsByDocument("987654")).thenReturn(false);
        when(passwordEncoder.encode("Password123*")).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).then(returnsFirstArg());

        User user = authService.register(new RegisterCommand("987654", "Nuevo", "Nuevo@helpflow.com",
                "Password123*", "Bogota", "Ventas"));

        assertThat(user.getRole()).isEqualTo(Role.USUARIO);
        assertThat(user.getPasswordHash()).isEqualTo("bcrypt-hash");
        assertThat(user.getEmail()).isEqualTo("nuevo@helpflow.com");
    }

    @Test
    void rechazaCorreoDuplicado() {
        when(userRepository.existsByEmail("ana@helpflow.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterCommand("987654", "Ana", "ana@helpflow.com",
                "Password123*", "MEDELLIN", "TI")))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void loginCorrectoDevuelveToken() {
        User user = TestData.activeUser(1L);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secreta", "hash")).thenReturn(true);
        when(tokenProvider.generateToken(user)).thenReturn("jwt");
        when(tokenProvider.expirationSeconds()).thenReturn(3600L);

        LoginResult result = authService.login(new LoginCommand(user.getEmail(), "secreta"));

        assertThat(result.token()).isEqualTo("jwt");
        assertThat(result.role()).isEqualTo(Role.USUARIO);
    }

    @Test
    void mismoErrorSiLaContrasenaEsIncorrectaOElCorreoNoExiste() {
        User user = TestData.activeUser(1L);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("mala", "hash")).thenReturn(false);
        when(userRepository.findByEmail("nadie@helpflow.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginCommand(user.getEmail(), "mala")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Credenciales inválidas");
        assertThatThrownBy(() -> authService.login(new LoginCommand("nadie@helpflow.com", "x")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Credenciales inválidas");
    }

    @Test
    void usuarioInactivoNoPuedeIniciarSesion() {
        User user = TestData.inactiveUser(2L);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secreta", "hash")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(new LoginCommand(user.getEmail(), "secreta")))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
