package com.helpflow.ticket.infrastructure.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "helpflow.jwt")
public record JwtProperties(@NotBlank String secret, @Positive long expirationSeconds) {
}
