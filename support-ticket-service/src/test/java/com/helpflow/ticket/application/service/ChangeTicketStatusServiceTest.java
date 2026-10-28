package com.helpflow.ticket.application.service;

import com.helpflow.ticket.application.port.in.ChangeTicketStatusUseCase.ChangeStatusCommand;
import com.helpflow.ticket.application.port.out.TechnicianRepositoryPort;
import com.helpflow.ticket.application.port.out.TicketHistoryRepositoryPort;
import com.helpflow.ticket.application.port.out.TicketMetricsPort;
import com.helpflow.ticket.application.port.out.TicketRepositoryPort;
import com.helpflow.ticket.domain.exception.ForbiddenOperationException;
import com.helpflow.ticket.domain.exception.InvalidStateTransitionException;
import com.helpflow.ticket.domain.model.CurrentUser;
import com.helpflow.ticket.domain.model.Technician;
import com.helpflow.ticket.domain.model.TestData;
import com.helpflow.ticket.domain.model.Ticket;
import com.helpflow.ticket.domain.model.TicketHistory;
import com.helpflow.ticket.domain.model.enums.Category;
import com.helpflow.ticket.domain.model.enums.HistoryAction;
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

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChangeTicketStatusServiceTest {

    private static final CurrentUser TEC05 = new CurrentUser(105L, "tec05@helpflow.com", Role.TECNICO);
    private static final CurrentUser ANA = new CurrentUser(1L, "ana@helpflow.com", Role.USUARIO);
    /** El reloj del servicio va 2 h por delante de la creación del ticket. */
    private static final Duration ELAPSED = Duration.ofHours(2);

    @Mock
    private TicketRepositoryPort ticketRepository;
    @Mock
    private TechnicianRepositoryPort technicianRepository;
    @Mock
    private TicketHistoryRepositoryPort historyRepository;
    @Mock
    private QueryTicketService queryTicketService;
    @Mock
    private TicketMetricsPort metrics;

    private ChangeTicketStatusService service;
    private Technician tec005;

    @BeforeEach
    void setUp() {
        service = new ChangeTicketStatusService(ticketRepository, technicianRepository, historyRepository,
                queryTicketService, metrics, Clock.fixed(TestData.NOW.plus(ELAPSED), ZoneOffset.UTC));
        tec005 = TestData.technician(5L, "TEC-005", Category.RED, "MEDELLIN", 1, 5);
        when(technicianRepository.findById(5L)).thenReturn(Optional.of(tec005));
        when(ticketRepository.save(any())).then(returnsFirstArg());
    }

    private Ticket givenTicket(TicketStatus status, Long technicianId, CurrentUser actor) {
        Ticket ticket = TestData.ticketInStatus(10L, status, technicianId);
        when(queryTicketService.getById(10L, actor)).thenReturn(ticket);
        return ticket;
    }

    @Test
    void elTecnicoIniciaLaAtencionYSeRegistraLaPrimeraRespuesta() {
        givenTicket(TicketStatus.ASIGNADO, 5L, TEC05);

        Ticket result = service.changeStatus(new ChangeStatusCommand(10L, TicketStatus.EN_PROGRESO, "Revisando"), TEC05);

        assertThat(result.getStatus()).isEqualTo(TicketStatus.EN_PROGRESO);
        assertThat(result.getFirstResponseAt()).isEqualTo(TestData.NOW.plus(ELAPSED));
        assertThat(tec005.getActiveTickets()).isEqualTo(1); // iniciar no cambia la carga
        verify(technicianRepository, never()).save(any());
        verify(metrics).statusChanged(TicketStatus.EN_PROGRESO);
        verify(metrics).firstResponse(ELAPSED);

        ArgumentCaptor<TicketHistory> history = ArgumentCaptor.forClass(TicketHistory.class);
        verify(historyRepository).save(history.capture());
        assertThat(history.getValue().action()).isEqualTo(HistoryAction.STATUS_CHANGED);
        assertThat(history.getValue().fromStatus()).isEqualTo(TicketStatus.ASIGNADO);
        assertThat(history.getValue().toStatus()).isEqualTo(TicketStatus.EN_PROGRESO);
        assertThat(history.getValue().performedBy()).isEqualTo(105L);
    }

    @Test
    void resolverLiberaLaCargaDelTecnicoYMideLaResolucion() {
        givenTicket(TicketStatus.EN_PROGRESO, 5L, TEC05);

        Ticket result = service.changeStatus(new ChangeStatusCommand(10L, TicketStatus.RESUELTO, "Listo"), TEC05);

        assertThat(result.getResolvedAt()).isEqualTo(TestData.NOW.plus(ELAPSED));
        assertThat(tec005.getActiveTickets()).isZero();
        verify(technicianRepository).save(tec005);
        verify(metrics).resolution(ELAPSED);
    }

    @Test
    void reabrirUnTicketResueltoVuelveASumarCargaAlTecnico() {
        tec005 = TestData.technician(5L, "TEC-005", Category.RED, "MEDELLIN", 0, 5);
        when(technicianRepository.findById(5L)).thenReturn(Optional.of(tec005));
        givenTicket(TicketStatus.RESUELTO, 5L, ANA);

        Ticket result = service.changeStatus(new ChangeStatusCommand(10L, TicketStatus.EN_PROGRESO, "Sigue fallando"), ANA);

        assertThat(result.getStatus()).isEqualTo(TicketStatus.EN_PROGRESO);
        assertThat(result.getResolvedAt()).isNull();
        assertThat(tec005.getActiveTickets()).isEqualTo(1);
        verify(technicianRepository).save(tec005);
    }

    @Test
    void cancelarUnTicketAbiertoSinTecnicoNoTocaNingunaCarga() {
        givenTicket(TicketStatus.ABIERTO, null, ANA);

        Ticket result = service.changeStatus(new ChangeStatusCommand(10L, TicketStatus.CANCELADO, "Ya no aplica"), ANA);

        assertThat(result.getStatus()).isEqualTo(TicketStatus.CANCELADO);
        verify(technicianRepository, never()).findById(any());
        verify(metrics).statusChanged(TicketStatus.CANCELADO);
        verify(metrics, never()).resolution(any());
    }

    @Test
    void cerrarUnTicketResueltoNoLiberaCargaDosVeces() {
        givenTicket(TicketStatus.RESUELTO, 5L, ANA);

        service.changeStatus(new ChangeStatusCommand(10L, TicketStatus.CERRADO, "Conforme"), ANA);

        assertThat(tec005.getActiveTickets()).isEqualTo(1);
        verify(technicianRepository, never()).save(any());
    }

    @Test
    void saltarEstadosLanza409YNoGuardaNada() {
        givenTicket(TicketStatus.ASIGNADO, 5L, TEC05);

        assertThatThrownBy(() -> service.changeStatus(
                new ChangeStatusCommand(10L, TicketStatus.RESUELTO, "Saltando estados"), TEC05))
                .isInstanceOf(InvalidStateTransitionException.class);
        verify(ticketRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
        verify(metrics, never()).statusChanged(any());
    }

    @Test
    void elUsuarioNoPuedeIniciarLaAtencion() {
        givenTicket(TicketStatus.ASIGNADO, 5L, ANA);

        assertThatThrownBy(() -> service.changeStatus(
                new ChangeStatusCommand(10L, TicketStatus.EN_PROGRESO, "Lo inicio yo"), ANA))
                .isInstanceOf(ForbiddenOperationException.class);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void siNoPuedeVerElTicketNoCambiaNada() {
        when(queryTicketService.getById(10L, ANA)).thenThrow(new ForbiddenOperationException("No tiene acceso"));

        assertThatThrownBy(() -> service.changeStatus(
                new ChangeStatusCommand(10L, TicketStatus.CANCELADO, "Ticket ajeno"), ANA))
                .isInstanceOf(ForbiddenOperationException.class);
        verify(ticketRepository, never()).save(any());
    }
}
