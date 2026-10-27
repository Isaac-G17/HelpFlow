package com.helpflow.ticket.infrastructure.config;

import io.micrometer.observation.ObservationRegistry;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    /**
     * Se usa RestClient.builder() con SimpleClientHttpRequestFactory (HttpURLConnection) en lugar del
     * RestClient.Builder autoconfigurado, que crea un java.net.http.HttpClient: en algunos equipos Windows
     * ese cliente falla al arrancar ("Unable to establish loopback connection"). Los timeouts quedan explícitos.
     */
    @Bean
    public RestClient dispatchRestClient(ObservationRegistry observationRegistry,
                                         @Value("${helpflow.dispatch.base-url}") String baseUrl,
                                         @Value("${helpflow.dispatch.connect-timeout}") Duration connectTimeout,
                                         @Value("${helpflow.dispatch.read-timeout}") Duration readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .observationRegistry(observationRegistry)
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
