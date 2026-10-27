package com.helpflow.ticket.infrastructure.config;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Cliente HTTP hacia technician-dispatch-service. Parte del RestClient.Builder autoconfigurado por Spring Boot,
 * que ya trae la fábrica y los timeouts de spring.http.client.* (conexión 2 s, lectura 3 s) y las métricas
 * http.client.requests. Aquí solo se agrega la URL base y la propagación del traceId.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient dispatchRestClient(RestClient.Builder builder,
                                         @Value("${helpflow.dispatch.base-url}") String baseUrl) {
        return builder
                .baseUrl(baseUrl)
                // Propaga el traceId para poder seguir una petición entre los dos microservicios.
                .requestInterceptor((request, body, execution) -> {
                    String traceId = MDC.get(TraceIdFilter.MDC_KEY);
                    if (traceId != null) {
                        request.getHeaders().add(TraceIdFilter.HEADER, traceId);
                    }
                    return execution.execute(request, body);
                })
                .build();
    }
}
