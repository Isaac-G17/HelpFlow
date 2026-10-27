package com.helpflow.ticket.infrastructure.adapter.in.rest.dto;

import com.helpflow.ticket.domain.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** DTOs de /auth. */
public final class AuthDtos {

    public static final String CORPORATE_EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@helpflow\\.com$";

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank @Pattern(regexp = "\\d{6,15}", message = "debe tener entre 6 y 15 dígitos") String document,
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Email
            @Pattern(regexp = CORPORATE_EMAIL_REGEX, message = "debe ser un correo corporativo @helpflow.com")
            String email,
            @NotBlank @Size(min = 8, max = 72, message = "debe tener entre 8 y 72 caracteres") String password,
            @NotBlank @Size(max = 60) String sede,
            @NotBlank @Size(max = 60) String area
    ) {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {
    }

    public record LoginResponse(String token, String tokenType, long expiresIn, Role role) {
    }
}
