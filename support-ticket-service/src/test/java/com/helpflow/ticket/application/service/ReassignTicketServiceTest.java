package com.helpflow.ticket.application.service;

import com.helpflow.ticket.application.port.in.ReassignTicketUseCase.ReassignTicketCommand;
import com.helpflow.ticket.application.port.out.AssignmentRepositoryPort;
import com.helpflow.ticket.application.port.out.TechnicianRepositoryPort;
import com.helpflow.ticket.application.port.out.TicketHistoryRepositoryPort;
import com.helpflow.ticket.application.port.out.TicketMetricsPort;
import com.helpflow.ticket.application.port.out.TicketRepositoryPort;
import com.helpflow.ticket.domain.exception.BusinessRuleException;
import com.helpflow.ticket.domain.exception.InactiveUserException;
import com.helpflow.ticket.domain.exception.InvalidStateTransitionException;
import com.helpflow.ticket.domain.exception.ResourceNotFoundException;
import com.helpflow.ticket.domain.model.Assignment;
import com.helpflow.ticket.domain.model.CurrentUser;
import com.helpflow.ticket.domain.model.Technician;
import com.helpflow.ticket.domain.model.TestData;
import com.helpflow.ticket.domain.model.Ticket;
import com.helpflow.ticket.domain.model.TicketHistory;
import com.helpflow.ticket.domain.model.enums.AccountStatus;
import com.helpflow.ticket.domain.model.enums.AssignmentOrigin;
import com.helpflow.ticket.domain.model.enums.Category;
import com.helpflow.ticket.domain.model.enums.HistoryAction;
import com.helpflow.ticket.domain.model.enums.Priority;
import com.helpflow.ticket.domain.model.enums.Role;
import com.helpflow.ticket.domain.model.enums.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReassignTicketServiceTest {

    private static final CurrentUser ADMIN = new CurrentUser(99L, "admin@helpflow.com", Role.ADMIN);

    @Mock
    private TicketRepositoryPort ticketRepository;
    @Mock
    private TechnicianRepositoryPort technicianRepository;
    @Mock
    private AssignmentRepositoryPort assignmentRepository;
    @Mock
    private TicketHistoryRepositoryPort historyRepository;
    @Mock
    private TicketMetricsPort metrics;

    private ReassignTicketService service;
    private Technician tec005;
    private Technician tec006;

    @BeforeEach
    void setUp() {
        service = new ReassignTicketService(ticketRepository, technicianRepository, assignmentRepository,
                historyRepository, metrics, Clock.fixed(TestData.NOW, ZoneOffset.UTC));
        tec005 = TestData.technician(5L, "TEC-005", Category.RED, "MEDELLIN", 1, 5);
        tec006 = TestData.technician(6L, "TEC-006", Category.RED, "BOGOTA", 0, 5);
        when(technicianRepository.findById(5L)).thenReturn(Optional.of(tec005));
        when(technicianRepository.findById(6L)).thenReturn(Optional.of(tec006));
        when(ticketRepository.save(any())).then(returnsFirstArg());
        when(technicianRepository.save(any())).then(returnsFirstArg());
    }

    @Test
    void reasignaMoviendoLaCargaYDejandoUnaSolaAsignacionActiva() {
        Ticket ticket = TestData.ticketInStatus(10L, TicketStatus.EN_PROGRESO, 5L);
        Assignment previousAssignment = Assignment.fallback(10L, 5L, TestData.NOW);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(assignmentRepository.findActiveByTicketId(10L)).thenReturn(Optional.of(previousAssignment));

        Ticket result = service.reassign(new ReassignTicketCommand(10L, 6L, "TEC-005 está de vacaciones"), ADMIN);

        assertThat(result.getTechnicianId()).isEqualTo(6L);
        assertThat(result.getStatus()).isEqualTo(TicketStatus.ASIGNADO);
        assertThat(tec005.getActiveTickets()).isZero();
        assertThat(tec006.getActiveTickets()).isEqualTo(1);
        verify(technicianRepository).save(tec005);
        verify(technicianRepository).save(tec006);

        // Primero se desactiva la anterior y luego se guarda la nueva (índice único de asignación activa).
        ArgumentCaptor<Assignment> assignments = ArgumentCaptor.forClass(Assignment.class);
        verify(assignmentRepository, times(2)).save(assignments.capture());
        assertThat(assignments.getAllValues().get(0)).isSameAs(previousAssignment);
        assertThat(previousAssignment.isActive()).isFalse();
        assertThat(assignments.getAllValues().get(1).getOrigin()).isEqualTo(AssignmentOrigin.MANUAL);
        assertThat(assignments.getAllValues().get(1).getTechnicianId()).isEqualTo(6L);

        ArgumentCaptor<TicketHistory> history = ArgumentCaptor.forClass(TicketHistory.class);
        verify(historyRepository).save(history.capture());
        assertThat(history.getValue().action()).isEqualTo(HistoryAction.REASSIGNED);
        assertThat(history.getValue().fromStatus()).isEqualTo(TicketStatus.EN_PROGRESO);
        assertThat(history.getValue().observation()).contains("TEC-006", "vacaciones");
        assertThat(history.getValue().performedBy()).isEqualTo(99L);
        verify(metrics).ticketAssigned(AssignmentOrigin.MANUAL);
    }

    @Test
    void unTicketAbiertoSinTecnicoSeAsignaDirectamente() {
        Ticket ticket = TestData.openTicket(10L, Category.RED, Priority.ALTA, "MEDELLIN");
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(assignmentRepository.findActiveByTicketId(10L)).thenReturn(Optional.empty());

        Ticket result = service.reassign(new ReassignTicketCommand(10L, 6L, "Asignación manual del admin"), ADMIN);

        assertThat(result.getStatus()).isEqualTo(TicketStatus.ASIGNADO);
        assertThat(result.getTechnicianId()).isEqualTo(6L);
        assertThat(tec006.getActiveTickets()).isEqualTo(1);
        assertThat(tec005.getActiveTickets()).isEqualTo(1); // no se tocó a nadie más
        verify(technicianRepository, never()).save(tec005);
    }

    @Test
    void noReasignaAlMismoTecnico() {
        when(ticketRepository.findById(10L))
                .thenReturn(Optional.of(TestData.ticketInStatus(10L, TicketStatus.ASIGNADO, 5L)));

        assertThatThrownBy(() -> service.reassign(new ReassignTicketCommand(10L, 5L, "Mismo técnico"), ADMIN))
                .isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void noReasignaUnTicketCerrado() {
        when(ticketRepository.findById(10L))
                .thenReturn(Optional.of(TestData.ticketInStatus(10L, TicketStatus.CERRADO, 5L)));

        assertThatThrownBy(() -> service.reassign(new ReassignTicketCommand(10L, 6L, "Ya estaba cerrado"), ADMIN))
                .isInstanceOf(InvalidStateTransitionException.class);
        verify(historyRepository, never()).save(any());
    }

    @Test
    void noReasignaAUnTecnicoInactivo() {
        Technician inactive = new Technician(7L, "TEC-007", 107L, "2007", "Tecnico 7", new BigDecimal("3000000.00"),
                LocalDate.of(2022, 1, 10), Category.RED, "MEDELLIN", AccountStatus.INACTIVO, 5, 0, 0L);
        when(technicianRepository.findById(7L)).thenReturn(Optional.of(inactive));
        when(ticketRepository.findById(10L))
                .thenReturn(Optional.of(TestData.ticketInStatus(10L, TicketStatus.ASIGNADO, 5L)));

        assertThatThrownBy(() -> service.reassign(new ReassignTicketCommand(10L, 7L, "Técnico inactivo"), ADMIN))
                .isInstanceOf(InactiveUserException.class);
        assertThat(tec005.getActiveTickets()).isEqualTo(1);
    }

    @Test
    void tecnicoInexistenteDevuelve404() {
        when(ticketRepository.findById(10L))
                .thenReturn(Optional.of(TestData.ticketInStatus(10L, TicketStatus.ASIGNADO, 5L)));
        when(technicianRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reassign(new ReassignTicketCommand(10L, 404L, "No existe"), ADMIN))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
