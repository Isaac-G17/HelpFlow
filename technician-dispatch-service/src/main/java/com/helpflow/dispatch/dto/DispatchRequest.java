package com.helpflow.dispatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record DispatchRequest(
        @NotNull @Positive Long ticketId,
        @NotBlank
        @Pattern(regexp = "HARDWARE|SOFTWARE|RED|ACCESOS|OTRO",
                message = "debe ser HARDWARE, SOFTWARE, RED, ACCESOS u OTRO")
        String categoria,
        @NotBlank
        @Pattern(regexp = "BAJA|MEDIA|ALTA|CRITICA",
                message = "debe ser BAJA, MEDIA, ALTA o CRITICA")
        String prioridad
) {
}
