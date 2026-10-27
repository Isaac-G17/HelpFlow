package com.helpflow.ticket.domain.model;

import com.helpflow.ticket.domain.model.enums.AccountStatus;
import com.helpflow.ticket.domain.model.enums.Category;
import com.helpflow.ticket.domain.model.enums.Priority;
import com.helpflow.ticket.domain.model.enums.Role;
import com.helpflow.ticket.domain.model.enums.TicketStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Fábricas de objetos de dominio para las pruebas (sin pasar por los TODO). */
public final class TestData {

    public static final Instant NOW = Instant.parse("2026-01-15T10:00:00Z");

    private TestData() {
    }

    public static User activeUser(Long id) {
        return new User(id, "100" + id, "Usuario " + id, "user" + id + "@helpflow.com", "MEDELLIN", "Ventas",
                AccountStatus.ACTIVO, "hash", Role.USUARIO, NOW);
    }

    public static User inactiveUser(Long id) {
        return new User(id, "100" + id, "Usuario " + id, "user" + id + "@helpflow.com", "MEDELLIN", "Ventas",
                AccountStatus.INACTIVO, "hash", Role.USUARIO, NOW);
    }

    public static Technician technician(Long id, String code, Category specialty, String sede, int active, int max) {
        return new Technician(id, code, 100 + id, "200" + id, "Tecnico " + id, new BigDecimal("3000000.00"),
                LocalDate.of(2022, 1, 10), specialty, sede, AccountStatus.ACTIVO, max, active, 0L);
    }

    /** Ticket ABIERTO creado en NOW con SLA de 8 h (ALTA). */
    public static Ticket openTicket(Long id, Category category, Priority priority, String sede) {
        return new Ticket(id, 1L, "Sin conexión VPN", "No conecta la VPN desde ayer", category, priority, sede,
                "LT-0231", TicketStatus.ABIERTO, null, false, NOW, NOW.plusSeconds(8 * 3600), null, null, 0L);
    }

    public static Ticket ticketInStatus(Long id, TicketStatus status, Long technicianId) {
        return new Ticket(id, 1L, "Sin conexión VPN", "No conecta la VPN desde ayer", Category.RED, Priority.ALTA,
                "MEDELLIN", "LT-0231", status, technicianId, false, NOW, NOW.plusSeconds(8 * 3600),
                null, null, 0L);
    }
}
