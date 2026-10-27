package com.helpflow.ticket.infrastructure.adapter.in.rest.dto;

import com.helpflow.ticket.domain.model.enums.AccountStatus;
import com.helpflow.ticket.domain.model.enums.Category;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class TechnicianDtos {

    private TechnicianDtos() {
    }

    public record CreateTechnicianRequest(
            @NotBlank @Pattern(regexp = "\\d{6,15}", message = "debe tener entre 6 y 15 dígitos") String document,
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Email
            @Pattern(regexp = AuthDtos.CORPORATE_EMAIL_REGEX, message = "debe ser un correo corporativo @helpflow.com")
            String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotBlank @Size(max = 60) String sede,
            @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal salary,
            @NotNull @PastOrPresent LocalDate affiliationDate,
            @NotNull Category specialty,
            @Min(1) @Max(20) int maxActiveTickets
    ) {
    }

    public record UpdateTechnicianRequest(
            @NotNull Category specialty,
            @NotBlank @Size(max = 60) String sede,
            @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal salary,
            @Min(1) @Max(20) int maxActiveTickets,
            @NotNull AccountStatus status
    ) {
    }

    public record TechnicianResponse(Long id, String code, Long userId, String document, String name,
                                     BigDecimal salary, LocalDate affiliationDate, Category specialty, String sede,
                                     AccountStatus status, int maxActiveTickets, int activeTickets) {
    }
}
