package com.helpflow.ticket.infrastructure.adapter.in.rest.dto;

import com.helpflow.ticket.domain.model.enums.AccountStatus;
import com.helpflow.ticket.domain.model.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class UserDtos {

    private UserDtos() {
    }

    public record UserResponse(Long id, String document, String name, String email, String sede, String area,
                               AccountStatus status, Role role, Instant createdAt) {
    }

    public record UpdateProfileRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Size(max = 60) String sede,
            @NotBlank @Size(max = 60) String area
    ) {
    }

    public record ChangeUserStatusRequest(@NotNull AccountStatus status) {
    }
}
