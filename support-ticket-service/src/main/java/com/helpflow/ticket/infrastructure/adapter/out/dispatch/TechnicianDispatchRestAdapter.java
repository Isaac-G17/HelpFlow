package com.helpflow.ticket.infrastructure.adapter.out.dispatch;

import com.helpflow.ticket.application.port.out.TechnicianDispatchPort;
import com.helpflow.ticket.domain.exception.TechnicianDispatchUnavailableException;
import com.helpflow.ticket.domain.model.Availability;
import com.helpflow.ticket.domain.model.Category;
import com.helpflow.ticket.domain.model.DispatchSuggestion;
import com.helpflow.ticket.domain.model.Priority;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Adapter de salida HTTP hacia technician-dispatch-service.
 * Timeouts: conexión 2 s, lectura 3 s (RestClientConfig). CUALQUIER falla se traduce a una excepción
 * de dominio -> 503, así el resto de la aplicación no conoce RestClient ni sus excepciones.
 */
@Slf4j
@Component
public class TechnicianDispatchRestAdapter implements TechnicianDispatchPort {

    static final String TIMER_NAME = "helpflow.dispatch.time";

    private final RestClient restClient;
    private final MeterRegistry meterRegistry;

    public TechnicianDispatchRestAdapter(@Qualifier("dispatchRestClient") RestClient restClient,
                                         MeterRegistry meterRegistry) {
        this.restClient = restClient;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public DispatchSuggestion requestTechnician(Long ticketId, Category category, Priority priority) {
        Timer.Sample sample = Timer.start(meterRegistry);
        String outcome = "success";
        try {
            DispatchResponseDto response = restClient.post()
                    .uri("/technician-dispatch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new DispatchRequestDto(ticketId, category.name(), priority.name()))
                    .retrieve()
                    .body(DispatchResponseDto.class);
            if (response == null || response.tecnicoId() == null || response.disponibilidad() == null) {
                throw new IllegalStateException("respuesta vacía o incompleta");
            }
            log.info("Dispatch ticketId={} -> tecnico={} horas={} disponibilidad={}", ticketId,
                    response.tecnicoId(), response.tiempoEstimadoHoras(), response.disponibilidad());
            return new DispatchSuggestion(ticketId, response.tecnicoId(),
                    Availability.valueOf(response.disponibilidad()), response.tiempoEstimadoHoras());
        } catch (Exception e) {
            outcome = "error";
            log.warn("technician-dispatch-service no disponible para ticketId={}: {}", ticketId, e.getMessage());
            throw new TechnicianDispatchUnavailableException(
                    "technician-dispatch-service no está disponible en este momento", e);
        } finally {
            sample.stop(Timer.builder(TIMER_NAME)
                    .description("Tiempo de respuesta de technician-dispatch-service")
                    .tag("outcome", outcome)
                    .register(meterRegistry));
        }
    }

    record DispatchRequestDto(Long ticketId, String categoria, String prioridad) {
    }

    record DispatchResponseDto(Long ticketId, String tecnicoId, String disponibilidad, int tiempoEstimadoHoras) {
    }
}
