package com.helpflow.ticket.infrastructure.config;

import com.helpflow.ticket.application.port.out.SlaSettingsPort;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/** helpflow.sla.* de application.yml, expuesto a la aplicación mediante SlaSettingsPort. */
@Validated
@ConfigurationProperties(prefix = "helpflow.sla")
public record SlaProperties(
        @Min(1) @Max(99) int escalationThresholdPercent,
        boolean schedulerEnabled,
        Duration escalationInterval
) implements SlaSettingsPort {
}
