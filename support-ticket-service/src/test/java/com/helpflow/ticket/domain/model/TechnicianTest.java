package com.helpflow.ticket.domain.model;

import com.helpflow.ticket.domain.exception.BusinessRuleException;
import com.helpflow.ticket.domain.exception.TechnicianCapacityExceededException;
import com.helpflow.ticket.domain.model.enums.Category;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Disabled("TODO: quita esta línea cuando implementes canAttend, hasCapacity, takeTicket y releaseTicket")
class TechnicianTest {

    @Test
    void atiendeSuEspecialidadYLaCategoriaOtro() {
        Technician tech = TestData.technician(1L, "TEC-005", Category.RED, "MEDELLIN", 0, 5);

        assertThat(tech.canAttend(Category.RED)).isTrue();
        assertThat(tech.canAttend(Category.OTRO)).isTrue();
        assertThat(tech.canAttend(Category.HARDWARE)).isFalse();
    }

    @Test
    void tomarTicketSubeLaCarga() {
        Technician tech = TestData.technician(1L, "TEC-005", Category.RED, "MEDELLIN", 0, 5);

        tech.takeTicket();

        assertThat(tech.getActiveTickets()).isEqualTo(1);
    }

    @Test
    void noSuperaLaCargaMaxima() {
        Technician tech = TestData.technician(1L, "TEC-005", Category.RED, "MEDELLIN", 5, 5);

        assertThat(tech.hasCapacity()).isFalse();
        assertThatThrownBy(tech::takeTicket).isInstanceOf(TechnicianCapacityExceededException.class);
    }

    @Test
    void laCargaNuncaQuedaNegativa() {
        Technician tech = TestData.technician(1L, "TEC-005", Category.RED, "MEDELLIN", 0, 5);

        assertThatThrownBy(tech::releaseTicket).isInstanceOf(BusinessRuleException.class);
    }
}
