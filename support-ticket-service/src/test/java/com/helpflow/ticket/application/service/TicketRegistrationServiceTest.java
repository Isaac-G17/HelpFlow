package com.helpflow.ticket.application.service;

import com.helpflow.ticket.application.port.in.CreateTicketUseCase.CreateTicketCommand;
import com.helpflow.ticket.application.port.out.SlaPolicyRepositoryPort;
import com.helpflow.ticket.application.port.out.TicketHistoryRepositoryPort;
import com.helpflow.ticket.application.port.out.TicketMetricsPort;
import com.helpflow.ticket.application.port.out.TicketRepositoryPort;
import com.helpflow.ticket.application.port.out.UserRepositoryPort;
import com.helpflow.ticket.domain.exception.InactiveUserException;
import com.helpflow.ticket.domain.exception.ResourceNotFoundException;
import com.helpflow.ticket.domain.model.CurrentUser;
import com.helpflow.ticket.domain.model.SlaPolicy;
import com.helpflow.ticket.domain.model.TestData;
import com.helpflow.ticket.domain.model.Ticket;
import com.helpflow.ticket.domain.model.TicketHistory;
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

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketRegistrationServiceTest {

    private static final CurrentUser ANA = new CurrentUser(1L, "ana@helpflow.com", Role.USUARIO);
    private static final CreateTicketCommand COMMAND = new CreateTicketCommand("Sin VPN",
            "No conecta la VPN corporativa", Category.RED, Priority.ALTA, " medellin ", "LT-0231");

    @Mock
    private TicketRepositoryPort ticketRepository;
    @Mock
    private TicketHistoryRepositoryPort historyRepository;
    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private SlaPolicyRepositoryPort slaPolicyRepository;
    @Mock
    private TicketMetricsPort metrics;

    private TicketRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new TicketRegistrationService(ticketRepository, historyRepository, userRepository,
                slaPolicyRepository, metrics, Clock.fixed(TestData.NOW, ZoneOffset.UTC));
    }

    @Test
    void registraElTicketAbiertoConSlaHistorialYMetrica() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(TestData.activeUser(1L)));
        when(slaPolicyRepository.findByPriority(Priority.ALTA)).thenReturn(Optional.of(new SlaPolicy(Priority.ALTA, 2, 8)));
        when(ticketRepository.save(any())).thenAnswer(inv -> withId(inv.getArgument(0), 10L));

        Ticket saved = service.open(COMMAND, ANA);

        assertThat(saved.getId()).isEqualTo(10L);
        assertThat(saved.getStatus()).isEqualTo(TicketStatus.ABIERTO);
        assertThat(saved.getRequesterId()).isEqualTo(1L);
        assertThat(saved.getSede()).isEqualTo("MEDELLIN");
        assertThat(saved.getSlaDeadline()).isEqualTo(TestData.NOW.plus(Duration.ofHours(8)));

        ArgumentCaptor<TicketHistory> history = ArgumentCaptor.forClass(TicketHistory.class);
        verify(historyRepository).save(history.capture());
        assertThat(history.getValue().ticketId()).isEqualTo(10L);
        assertThat(history.getValue().fromStatus()).isNull();
        assertThat(history.getValue().toStatus()).isEqualTo(TicketStatus.ABIERTO);
        assertThat(history.getValue().action()).isEqualTo(HistoryAction.CREATED);
        verify(metrics).ticketCreated(Category.RED, Priority.ALTA);
    }

    @Test
    void unUsuarioInactivoNoRegistraTickets() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(TestData.inactiveUser(1L)));
        when(slaPolicyRepository.findByPriority(Priority.ALTA)).thenReturn(Optional.of(new SlaPolicy(Priority.ALTA, 2, 8)));

        assertThatThrownBy(() -> service.open(COMMAND, ANA)).isInstanceOf(InactiveUserException.class);
        verify(ticketRepository, never()).save(any());
        verifyNoInteractions(historyRepository, metrics);
    }

    @Test
    void sinPoliticaDeSlaParaLaPrioridadDevuelve404() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(TestData.activeUser(1L)));
        when(slaPolicyRepository.findByPriority(Priority.ALTA)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.open(COMMAND, ANA))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ALTA");
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void solicitanteInexistenteDevuelve404() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.open(COMMAND, ANA)).isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(slaPolicyRepository, ticketRepository);
    }

    @Test
    void registraLaFallaDeDispatchSinCambiarElEstado() {
        service.recordDispatchFailure(10L, "timeout");

        ArgumentCaptor<TicketHistory> history = ArgumentCaptor.forClass(TicketHistory.class);
        verify(historyRepository).save(history.capture());
        assertThat(history.getValue().action()).isEqualTo(HistoryAction.DISPATCH_FAILED);
        assertThat(history.getValue().fromStatus()).isEqualTo(TicketStatus.ABIERTO);
        assertThat(history.getValue().toStatus()).isEqualTo(TicketStatus.ABIERTO);
        assertThat(history.getValue().observation()).contains("timeout");
        assertThat(history.getValue().performedBy()).isNull();
    }

    @Test
    void reloadDeUnTicketInexistenteDevuelve404() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reload(10L)).isInstanceOf(ResourceNotFoundException.class);
    }

    /** Simula el id que asigna la BD al guardar. */
    private static Ticket withId(Ticket t, Long id) {
        return new Ticket(id, t.getRequesterId(), t.getTitle(), t.getDescription(), t.getCategory(), t.getPriority(),
                t.getSede(), t.getAffectedAsset(), t.getStatus(), t.getTechnicianId(), t.isEscalated(),
                t.getCreatedAt(), t.getSlaDeadline(), t.getFirstResponseAt(), t.getResolvedAt(), 0L);
    }
}
