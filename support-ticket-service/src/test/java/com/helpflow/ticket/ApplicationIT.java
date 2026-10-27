package com.helpflow.ticket;

import com.fasterxml.jackson.databind.JsonNode;
import com.helpflow.ticket.application.port.out.TechnicianDispatchPort;
import com.helpflow.ticket.domain.exception.TechnicianDispatchUnavailableException;
import com.helpflow.ticket.domain.model.DispatchSuggestion;
import com.helpflow.ticket.domain.model.enums.Availability;
import com.helpflow.ticket.domain.model.enums.Category;
import com.helpflow.ticket.domain.model.enums.Priority;
import com.helpflow.ticket.infrastructure.adapter.out.dispatch.TechnicianDispatchRestAdapter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración (Testcontainers + MockMvc). NO corren con "mvn test":
 *   mvn test -Dtest=ApplicationIT
 * Cada prueba crea sus propios datos con un sufijo único.
 */
class ApplicationIT extends AbstractIntegrationTest {

    /** Dispatch simulado: las IT no dependen de que el otro microservicio esté arriba. */
    @MockitoBean
    private TechnicianDispatchPort dispatchPort;

    @Autowired
    private RestClient.Builder restClientBuilder;

    @Autowired
    private MeterRegistry meterRegistry;

    // ======================= Infraestructura =======================

    @Test
    void healthEInfoSonPublicos() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
        mockMvc.perform(get("/actuator/info")).andExpect(status().isOk());
    }

    @Test
    void metricasSoloParaAdmin() throws Exception {
        String ana = login("ana@helpflow.com", SEED_PASSWORD);

        mockMvc.perform(get("/actuator/metrics").header(HttpHeaders.AUTHORIZATION, ana))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        mockMvc.perform(get("/actuator/metrics/helpflow.tickets.overdue").header(HttpHeaders.AUTHORIZATION, adminToken()))
                .andExpect(status().isOk());
    }

    @Test
    void prometheusExponeElGaugeDeTicketsVencidos() throws Exception {
        String body = mockMvc.perform(get("/actuator/prometheus").header(HttpHeaders.AUTHORIZATION, adminToken()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        // V3 siembra un ticket vencido.
        assertThat(body).contains("helpflow_tickets_overdue");
    }

    @Test
    void elAdapterDeDispatchTraduceUnaFallaDeRedAExcepcionDeDominio() {
        // Builder real autoconfigurado (spring.http.client.factory=simple) contra un puerto cerrado.
        TechnicianDispatchRestAdapter adapter = new TechnicianDispatchRestAdapter(
                restClientBuilder.baseUrl("http://localhost:1").build(), meterRegistry);

        assertThatThrownBy(() -> adapter.requestTechnician(1L, Category.RED, Priority.ALTA))
                .isInstanceOf(TechnicianDispatchUnavailableException.class);
        assertThat(meterRegistry.find("helpflow.dispatch.time").tag("outcome", "error").timer()).isNotNull();
    }

    // ======================= Autenticación =======================

    @Test
    void registroYLoginDeUnUsuarioNuevo() throws Exception {
        long n = unique();
        String email = "it" + n + "@helpflow.com";
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                        {"document":"%d","name":"Usuario IT","email":"%s","password":"Password123*",
                         "sede":"Medellin","area":"QA"}""".formatted(n, email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USUARIO"))
                .andExpect(jsonPath("$.sede").value("MEDELLIN"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        mockMvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, login(email, "Password123*")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void registroDuplicadoDevuelve409() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                        {"document":"%d","name":"Dup","email":"ana@helpflow.com","password":"Password123*",
                         "sede":"Medellin","area":"QA"}""".formatted(unique())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("https://helpflow.com/errors/duplicate"));
    }

    @Test
    void registroConCorreoNoCorporativoDevuelve400ConErroresPorCampo() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                        {"document":"12","name":"","email":"x@gmail.com","password":"123",
                         "sede":"Medellin","area":"QA"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.document").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.traceId", notNullValue()))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void loginIncorrectoDevuelve401ProblemDetail() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email":"ana@helpflow.com","password":"incorrecta"}"""))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Credenciales inválidas"))
                .andExpect(jsonPath("$.instance").value("/auth/login"));
    }

    @Test
    void sinTokenOTokenInvalidoDevuelve401() throws Exception {
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.traceId", notNullValue()));
        mockMvc.perform(get("/api/tickets").header(HttpHeaders.AUTHORIZATION, "Bearer token.falso.xyz"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Token inválido o expirado"));
    }

    @Test
    void usuarioInactivoNoPuedeIniciarSesion() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email":"pedro@helpflow.com","password":"Password123*"}"""))
                .andExpect(status().isUnauthorized());
    }

    // ======================= Roles =======================

    @Test
    void usuarioNoPuedeGestionarTecnicosNiSla() throws Exception {
        String ana = login("ana@helpflow.com", SEED_PASSWORD);

        mockMvc.perform(get("/api/technicians").header(HttpHeaders.AUTHORIZATION, ana))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/sla-policies").header(HttpHeaders.AUTHORIZATION, ana))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCreaTecnicoConCodigoAutogenerado() throws Exception {
        long n = unique();
        mockMvc.perform(post("/api/technicians").header(HttpHeaders.AUTHORIZATION, adminToken())
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"document":"%d","name":"Tecnico IT","email":"tec%d@helpflow.com",
                                 "password":"Password123*","sede":"Cali","specialty":"SOFTWARE","maxActiveTickets":3}
                                """.formatted(n, n)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(org.hamcrest.Matchers.startsWith("TEC-")))
                .andExpect(jsonPath("$.activeTickets").value(0));
    }

    @Test
    void adminConfiguraSlaConValidacionCruzada() throws Exception {
        String admin = adminToken();
        mockMvc.perform(put("/api/sla-policies/BAJA").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"responseHours":10,"resolutionHours":5}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.resolutionHours").exists());
        mockMvc.perform(put("/api/sla-policies/BAJA").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"responseHours":8,"resolutionHours":72}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resolutionHours").value(72));
    }

    // ======================= Tickets: listados (ya implementados) =======================

    @Test
    void adminListaTodosLosTicketsConSolicitanteYTecnico() throws Exception {
        mockMvc.perform(get("/api/tickets").header(HttpHeaders.AUTHORIZATION, adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].requesterName", hasItem("Ana Gomez")))
                .andExpect(jsonPath("$.content[*].technicianCode", hasItem("TEC-005")));
    }

    @Test
    void usuarioSoloVeSusTickets() throws Exception {
        String luis = login("luis@helpflow.com", SEED_PASSWORD);
        mockMvc.perform(get("/api/tickets").header(HttpHeaders.AUTHORIZATION, luis))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].requesterName", everyItem(is("Luis Perez"))));
    }

    @Test
    void tecnicoSoloVeLosTicketsAsignados() throws Exception {
        String tec05 = login("tec05@helpflow.com", SEED_PASSWORD);
        mockMvc.perform(get("/api/tickets").header(HttpHeaders.AUTHORIZATION, tec05))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].technicianCode", everyItem(is("TEC-005"))));
    }

    @Test
    void enumInvalidoEnFiltroDevuelve400() throws Exception {
        mockMvc.perform(get("/api/tickets?status=INVENTADO").header(HttpHeaders.AUTHORIZATION, adminToken()))
                .andExpect(status().isBadRequest());
    }

    // ======================= Tickets: flujo completo (activar al terminar los TODO) =======================

    @Test
    @Disabled("TODO: activar cuando implementes CreateTicketService + AssignTicketService + dominio")
    void crearTicketLoAsignaConElTecnicoSugerido() throws Exception {
        when(dispatchPort.requestTechnician(anyLong(), any(Category.class), any(Priority.class)))
                .thenAnswer(inv -> new DispatchSuggestion(inv.getArgument(0), "TEC-001", Availability.ALTA, 1));
        String ana = login("ana@helpflow.com", SEED_PASSWORD);

        String body = mockMvc.perform(post("/api/tickets").header(HttpHeaders.AUTHORIZATION, ana)
                        .contentType(MediaType.APPLICATION_JSON).content(newTicket("HARDWARE", "ALTA", "MEDELLIN")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ASIGNADO"))
                .andExpect(jsonPath("$.slaDeadline", notNullValue()))
                .andReturn().getResponse().getContentAsString();
        long id = json(body).get("id").asLong();

        mockMvc.perform(get("/api/tickets/" + id + "/history").header(HttpHeaders.AUTHORIZATION, ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].action", hasItem("CREATED")))
                .andExpect(jsonPath("$[*].action", hasItem("ASSIGNED")));
    }

    @Test
    @Disabled("TODO: activar cuando implementes CreateTicketService y AutoAssignTicketService")
    void siDispatchEstaCaidoElTicketQuedaAbiertoYAutoAssignDevuelve503() throws Exception {
        when(dispatchPort.requestTechnician(anyLong(), any(), any()))
                .thenThrow(new TechnicianDispatchUnavailableException("caído", null));
        String ana = login("ana@helpflow.com", SEED_PASSWORD);

        String body = mockMvc.perform(post("/api/tickets").header(HttpHeaders.AUTHORIZATION, ana)
                        .contentType(MediaType.APPLICATION_JSON).content(newTicket("RED", "MEDIA", "MEDELLIN")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ABIERTO"))
                .andReturn().getResponse().getContentAsString();
        long id = json(body).get("id").asLong();

        mockMvc.perform(post("/api/tickets/" + id + "/auto-assign").header(HttpHeaders.AUTHORIZATION, adminToken()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.type").value("https://helpflow.com/errors/dispatch-unavailable"));
    }

    @Test
    @Disabled("TODO: activar cuando implementes QueryTicketService.ensureCanView")
    void unUsuarioNoPuedeVerElTicketDeOtro() throws Exception {
        String luis = login("luis@helpflow.com", SEED_PASSWORD);
        JsonNode anaTickets = json(mockMvc.perform(get("/api/tickets")
                        .header(HttpHeaders.AUTHORIZATION, login("ana@helpflow.com", SEED_PASSWORD)))
                .andReturn().getResponse().getContentAsString());
        long anaTicketId = anaTickets.get("content").get(0).get("id").asLong();

        mockMvc.perform(get("/api/tickets/" + anaTicketId).header(HttpHeaders.AUTHORIZATION, luis))
                .andExpect(status().isForbidden());
    }

    @Test
    @Disabled("TODO: activar cuando implementes ChangeTicketStatusService y el dominio")
    void transicionesValidasEInvalidas() throws Exception {
        when(dispatchPort.requestTechnician(anyLong(), any(Category.class), any(Priority.class)))
                .thenAnswer(inv -> new DispatchSuggestion(inv.getArgument(0), "TEC-003", Availability.ALTA, 1));
        String ana = login("ana@helpflow.com", SEED_PASSWORD);
        String tec03 = login("tec03@helpflow.com", SEED_PASSWORD);
        long id = json(mockMvc.perform(post("/api/tickets").header(HttpHeaders.AUTHORIZATION, ana)
                        .contentType(MediaType.APPLICATION_JSON).content(newTicket("SOFTWARE", "MEDIA", "MEDELLIN")))
                .andExpect(jsonPath("$.status").value("ASIGNADO"))
                .andReturn().getResponse().getContentAsString()).get("id").asLong();

        // Saltar estados -> 409
        mockMvc.perform(patch("/api/tickets/" + id + "/status").header(HttpHeaders.AUTHORIZATION, tec03)
                        .contentType(MediaType.APPLICATION_JSON).content(statusChange("RESUELTO")))
                .andExpect(status().isConflict());
        // El usuario no inicia la atención -> 403
        mockMvc.perform(patch("/api/tickets/" + id + "/status").header(HttpHeaders.AUTHORIZATION, ana)
                        .contentType(MediaType.APPLICATION_JSON).content(statusChange("EN_PROGRESO")))
                .andExpect(status().isForbidden());
        // Flujo feliz
        mockMvc.perform(patch("/api/tickets/" + id + "/status").header(HttpHeaders.AUTHORIZATION, tec03)
                        .contentType(MediaType.APPLICATION_JSON).content(statusChange("EN_PROGRESO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstResponseAt", notNullValue()));
        mockMvc.perform(patch("/api/tickets/" + id + "/status").header(HttpHeaders.AUTHORIZATION, tec03)
                        .contentType(MediaType.APPLICATION_JSON).content(statusChange("RESUELTO")))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/tickets/" + id + "/status").header(HttpHeaders.AUTHORIZATION, ana)
                        .contentType(MediaType.APPLICATION_JSON).content(statusChange("CERRADO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CERRADO"));
    }

    private static String newTicket(String category, String priority, String sede) {
        return """
                {"title":"Ticket IT %d","description":"Descripción de prueba de integración",
                 "category":"%s","priority":"%s","sede":"%s","affectedAsset":"PC-IT"}
                """.formatted(unique(), category, priority, sede);
    }

    private static String statusChange(String status) {
        return """
                {"status":"%s","observation":"Cambio realizado en prueba de integración"}""".formatted(status);
    }
}
