package com.helpflow.ticket.infrastructure.config;

import com.helpflow.ticket.application.port.out.TicketRepositoryPort;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class MetricsConfig {

    /** Tickets activos con el SLA vencido. Se calcula con un COUNT en cada lectura de la métrica. */
    @Bean
    public Gauge overdueTicketsGauge(MeterRegistry registry, TicketRepositoryPort ticketRepository, Clock clock) {
        return Gauge.builder("helpflow.tickets.overdue", () -> ticketRepository.countOverdue(clock.instant()))
                .description("Tickets activos cuya fecha límite de SLA ya pasó")
                .register(registry);
    }
}
