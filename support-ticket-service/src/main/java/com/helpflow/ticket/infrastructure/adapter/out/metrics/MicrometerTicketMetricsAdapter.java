package com.helpflow.ticket.infrastructure.adapter.out.metrics;

import com.helpflow.ticket.application.port.out.TicketMetricsPort;
import com.helpflow.ticket.domain.model.AssignmentOrigin;
import com.helpflow.ticket.domain.model.Category;
import com.helpflow.ticket.domain.model.Priority;
import com.helpflow.ticket.domain.model.TicketStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Métricas de negocio con prefijo helpflow. Los Timer exponen count, sum y max: el tiempo medio
 * de primera respuesta / resolución es sum / count (en Prometheus: rate(_sum) / rate(_count)).
 */
@Component
@RequiredArgsConstructor
public class MicrometerTicketMetricsAdapter implements TicketMetricsPort {

    private final MeterRegistry registry;

    @Override
    public void ticketCreated(Category category, Priority priority) {
        Counter.builder("helpflow.tickets.created")
                .description("Tickets creados por categoría y prioridad")
                .tag("category", category.name())
                .tag("priority", priority.name())
                .register(registry)
                .increment();
    }

    @Override
    public void ticketAssigned(AssignmentOrigin origin) {
        Counter.builder("helpflow.tickets.assigned")
                .description("Asignaciones por origen (DISPATCH, FALLBACK, MANUAL)")
                .tag("origin", origin.name())
                .register(registry)
                .increment();
    }

    @Override
    public void statusChanged(TicketStatus target) {
        Counter.builder("helpflow.tickets.status_changed")
                .description("Cambios de estado por estado destino")
                .tag("to", target.name())
                .register(registry)
                .increment();
    }

    @Override
    public void firstResponse(Duration elapsed) {
        Timer.builder("helpflow.tickets.first_response.time")
                .description("Tiempo desde la creación hasta EN_PROGRESO")
                .register(registry)
                .record(elapsed);
    }

    @Override
    public void resolution(Duration elapsed) {
        Timer.builder("helpflow.tickets.resolution.time")
                .description("Tiempo desde la creación hasta RESUELTO")
                .register(registry)
                .record(elapsed);
    }

    @Override
    public void ticketsEscalated(int count) {
        Counter.builder("helpflow.tickets.escalated")
                .description("Tickets escalados por SLA")
                .register(registry)
                .increment(count);
    }
}
