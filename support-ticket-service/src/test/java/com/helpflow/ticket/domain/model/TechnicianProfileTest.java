package com.helpflow.ticket.domain.model;

import com.helpflow.ticket.domain.exception.BusinessRuleException;
import com.helpflow.ticket.domain.model.enums.AccountStatus;
import com.helpflow.ticket.domain.model.enums.Category;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Creación y edición del técnico (ya implementadas; los TODO de carga están en TechnicianTest). */
class TechnicianProfileTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 1, 15);
    private final User user = TestData.activeUser(7L);

    @Test
    void creaTecnicoConDatosDelUsuarioSalarioYAfiliacion() {
        Technician tech = Technician.create("TEC-016", user, new BigDecimal("3500000.00"), LocalDate.of(2024, 5, 2),
                Category.RED, "barranquilla", 5, TODAY);

        assertThat(tech.getDocument()).isEqualTo(user.getDocument());
        assertThat(tech.getName()).isEqualTo(user.getName());
        assertThat(tech.getSalary()).isEqualByComparingTo("3500000");
        assertThat(tech.getSede()).isEqualTo("BARRANQUILLA");
        assertThat(tech.getStatus()).isEqualTo(AccountStatus.ACTIVO);
        assertThat(tech.getActiveTickets()).isZero();
    }

    @Test
    void elSalarioDebeSerMayorQueCero() {
        assertThatThrownBy(() -> Technician.create("TEC-016", user, BigDecimal.ZERO, LocalDate.of(2024, 5, 2),
                Category.RED, "MEDELLIN", 5, TODAY))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("salario");
    }

    @Test
    void laFechaDeAfiliacionNoPuedeSerFutura() {
        assertThatThrownBy(() -> Technician.create("TEC-016", user, new BigDecimal("3000000"), TODAY.plusDays(1),
                Category.RED, "MEDELLIN", 5, TODAY))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("afiliación");
    }

    @Test
    void editarPerfilActualizaSalarioYEstado() {
        Technician tech = TestData.technician(1L, "TEC-001", Category.HARDWARE, "MEDELLIN", 0, 5);

        tech.updateProfile(Category.HARDWARE, "Bogota", new BigDecimal("4200000"), 6, AccountStatus.INACTIVO);

        assertThat(tech.getSalary()).isEqualByComparingTo("4200000");
        assertThat(tech.getSede()).isEqualTo("BOGOTA");
        assertThat(tech.isActive()).isFalse();
    }
}
