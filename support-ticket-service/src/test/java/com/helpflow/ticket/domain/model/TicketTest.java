package com.helpflow.ticket.domain.model;

import com.helpflow.ticket.domain.exception.BusinessRuleException;
import com.helpflow.ticket.domain.exception.ForbiddenOperationException;
import com.helpflow.ticket.domain.exception.InactiveUserException;
import com.helpflow.ticket.domain.exception.InvalidStateTransitionException;
import com.helpflow.ticket.domain.model.enums.Category;
import com.helpflow.ticket.domain.model.enums.Priority;
import com.helpflow.ticket.domain.model.enums.Role;
import com.helpflow.ticket.domain.model.enums.TicketStatus;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static com.helpflow.ticket.domain.model.TestData.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class TicketTest {

    private final SlaPolicy alta = new SlaPolicy(Priority.ALTA, 2, 8);

    @Test
    void abreTicketEnEstadoAbiertoConFechaLimiteSegunPrioridad() {
        Ticket ticket = Ticket.open(TestData.activeUser(1L), "Sin VPN", "No conecta la VPN corporativa",
                Category.RED, Priority.ALTA, "medellin", "LT-0231", alta, NOW);

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.ABIERTO);
        assertThat(ticket.getSlaDeadline()).isEqualTo(NOW.plus(Duration.ofHours(8)));
        assertThat(ticket.getSede()).isEqualTo("MEDELLIN");
        assertThat(ticket.getTechnicianId()).isNull();
    }

    @Test
    void usuarioInactivoNoPuedeRegistrarTickets() {
        assertThatThrownBy(() -> Ticket.open(TestData.inactiveUser(1L), "Sin VPN", "No conecta la VPN",
                Category.RED, Priority.ALTA, "MEDELLIN", "LT-0231", alta, NOW))
                .isInstanceOf(InactiveUserException.class);
    }

    @Test
    void asignarPasaAAsignadoYSubeLaCargaDelTecnico() {
        Ticket ticket = TestData.openTicket(10L, Category.RED, Priority.ALTA, "MEDELLIN");
        Technician tech = TestData.technician(5L, "TEC-005", Category.RED, "MEDELLIN", 0, 5);

        ticket.assignTo(tech);

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.ASIGNADO);
        assertThat(ticket.getTechnicianId()).isEqualTo(5L);
        assertThat(tech.getActiveTickets()).isEqualTo(1);
    }

    @Test
    void noAsignaUnTecnicoDeOtraEspecialidad() {
        Ticket ticket = TestData.openTicket(10L, Category.RED, Priority.ALTA, "MEDELLIN");
        Technician tech = TestData.technician(1L, "TEC-001", Category.HARDWARE, "MEDELLIN", 0, 5);

        assertThatThrownBy(() -> ticket.assignTo(tech)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void elTecnicoIniciaLaAtencionYSeRegistraLaPrimeraRespuesta() {
        Ticket ticket = TestData.ticketInStatus(10L, TicketStatus.ASIGNADO, 5L);
        Instant later = NOW.plus(Duration.ofHours(1));

        TicketStatus previous = ticket.changeStatus(TicketStatus.EN_PROGRESO, Role.TECNICO, later);

        assertThat(previous).isEqualTo(TicketStatus.ASIGNADO);
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.EN_PROGRESO);
        assertThat(ticket.getFirstResponseAt()).isEqualTo(later);
    }

    @Test
    void elUsuarioNoPuedeIniciarLaAtencion() {
        Ticket ticket = TestData.ticketInStatus(10L, TicketStatus.ASIGNADO, 5L);

        assertThatThrownBy(() -> ticket.changeStatus(TicketStatus.EN_PROGRESO, Role.USUARIO, NOW))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void noSePuedeSaltarEstados() {
        Ticket ticket = TestData.openTicket(10L, Category.RED, Priority.ALTA, "MEDELLIN");

        assertThatThrownBy(() -> ticket.changeStatus(TicketStatus.RESUELTO, Role.ADMIN, NOW))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void resolverRegistraLaFechaDeResolucion() {
        Ticket ticket = TestData.ticketInStatus(10L, TicketStatus.EN_PROGRESO, 5L);
        Instant later = NOW.plus(Duration.ofHours(3));

        ticket.changeStatus(TicketStatus.RESUELTO, Role.TECNICO, later);

        assertThat(ticket.getResolvedAt()).isEqualTo(later);
    }

    @Test
    void detectaTicketsProximosAIncumplirElSla() {
        Ticket ticket = TestData.openTicket(10L, Category.RED, Priority.ALTA, "MEDELLIN"); // ventana de 8 h

        assertThat(ticket.isNearSlaBreach(NOW.plus(Duration.ofHours(4)), 20)).isFalse(); // queda 50 %
        assertThat(ticket.isNearSlaBreach(NOW.plus(Duration.ofHours(7)), 20)).isTrue();  // queda 12.5 %
        assertThat(ticket.isNearSlaBreach(NOW.plus(Duration.ofHours(9)), 20)).isTrue();  // ya vencido
    }

    @Test
    void escalarSubePrioridadUnaSolaVez() {
        Ticket ticket = TestData.openTicket(10L, Category.RED, Priority.ALTA, "MEDELLIN");

        Priority previous = ticket.escalate();

        assertThat(previous).isEqualTo(Priority.ALTA);
        assertThat(ticket.getPriority()).isEqualTo(Priority.CRITICA);
        assertThat(ticket.isEscalated()).isTrue();
        assertThatThrownBy(ticket::escalate).isInstanceOf(BusinessRuleException.class);
    }
}
