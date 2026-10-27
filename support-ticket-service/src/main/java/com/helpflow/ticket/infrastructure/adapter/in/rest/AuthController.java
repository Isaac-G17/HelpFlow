package com.helpflow.ticket.infrastructure.adapter.in.rest;

import com.helpflow.ticket.application.port.in.LoginUseCase;
import com.helpflow.ticket.application.port.in.RegisterUserUseCase;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.AuthDtos.LoginRequest;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.AuthDtos.LoginResponse;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.AuthDtos.RegisterRequest;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.UserDtos.UserResponse;
import com.helpflow.ticket.infrastructure.adapter.in.rest.mapper.RestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación")
@SecurityRequirements
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;
    private final RestMapper mapper;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra un usuario con rol USUARIO (correo @helpflow.com)")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return mapper.toResponse(registerUserUseCase.register(mapper.toCommand(request)));
    }

    @PostMapping("/login")
    @Operation(summary = "Devuelve un JWT. Úsalo en el botón Authorize de Swagger")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        LoginUseCase.LoginResult result = loginUseCase.login(mapper.toCommand(request));
        return new LoginResponse(result.token(), "Bearer", result.expiresInSeconds(), result.role());
    }
}
