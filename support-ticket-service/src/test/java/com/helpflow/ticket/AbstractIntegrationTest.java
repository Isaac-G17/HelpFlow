package com.helpflow.ticket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base de las pruebas de integración: Spring completo + MockMvc + PostgreSQL 16 real en un contenedor.
 * Flyway aplica V1..V3 sobre la BD del contenedor, igual que en producción.
 * El contenedor es static: se reutiliza en todas las pruebas de la clase (arranca una sola vez).
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureObservability(tracing = false) // en tests Boot desactiva el export a Prometheus; lo activamos
@Testcontainers
@TestPropertySource(properties = {
        "helpflow.sla.scheduler-enabled=false",
        "logging.structured.format.console=",
        "helpflow.dispatch.base-url=http://localhost:1"
})
public abstract class AbstractIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    /** Contraseña de todos los usuarios semilla (V3__datos_iniciales). */
    protected static final String SEED_PASSWORD = "Password123*";

    private static final AtomicLong SEQUENCE = new AtomicLong(System.currentTimeMillis() % 1_000_000);

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    /** Sufijo único para que cada prueba use sus propios datos (documento, correo...). */
    protected static long unique() {
        return SEQUENCE.incrementAndGet();
    }

    protected String login(String email, String password) throws Exception {
        String body = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Credentials(email, password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + objectMapper.readTree(body).get("token").asText();
    }

    protected String adminToken() throws Exception {
        return login("admin@helpflow.com", SEED_PASSWORD);
    }

    protected JsonNode json(String content) throws Exception {
        return objectMapper.readTree(content);
    }

    private record Credentials(String email, String password) {
    }
}
