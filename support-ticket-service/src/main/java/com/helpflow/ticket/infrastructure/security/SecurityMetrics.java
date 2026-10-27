package com.helpflow.ticket.infrastructure.security;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Métricas de seguridad: fallas de autenticación (401) y accesos denegados (403). */
@Component
@RequiredArgsConstructor
public class SecurityMetrics {

    private final MeterRegistry registry;

    /** @param reason bad_credentials | missing_token | invalid_token */
    public void authenticationFailure(String reason) {
        Counter.builder("helpflow.auth.failures")
                .description("Fallas de autenticación")
                .tag("reason", reason)
                .register(registry)
                .increment();
    }

    /** @param source role (Spring Security / @PreAuthorize) | ownership (recurso ajeno, caso de uso) */
    public void accessDenied(String source) {
        Counter.builder("helpflow.access.denied")
                .description("Accesos denegados")
                .tag("source", source)
                .register(registry)
                .increment();
    }
}
