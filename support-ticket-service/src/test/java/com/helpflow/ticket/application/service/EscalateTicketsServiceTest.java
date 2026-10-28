package com.helpflow.ticket.application.service;

import com.helpflow.ticket.application.port.out.SlaSettingsPort;
import com.helpflow.ticket.application.port.out.TicketHistoryRepositoryPort;
import com.helpflow.ticket.application.port.out.TicketMetricsPort;
import com.helpflow.ticket.application.port.out.TicketRepositoryPort;
import com.helpflow.ticket.domain.model.TestData;
import com.helpflow.ticket.domain.model.Ticket;
import com.helpflow.ticket.domain.model.TicketHistory;
import com.helpflow.ticket.domain.model.enums.Category;
import com.helpflow.ticket.domain.model.enums.HistoryAction;
import com.helpflow.ticket.domain.model.enums.Priority;
import com.helpflow.ticket.domain.model.enums.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EscalateTicketsServiceTest {

    @Mock
    private TicketRepositoryPort ticketRepository;
    @Mock
    private TicketHistoryRepositoryPort historyRepository;
    @Mock
    private SlaSettingsPort slaSettings;
    @Mock
    private TicketMetricsPort metrics;

    private EscalateTicketsService service;

    @BeforeEach
    void setUp() {
        service = new EscalateTicketsService(ticketRepository, historyRepository, slaSettings, metrics,
                Clock.fixed(TestData.NOW, ZoneOffset.UTC));
        when(slaSettings.escalationThresholdPercent()).thenReturn(20);
    }

    /** Ticket con ventana de SLA de 10 h al que le quedan {@code remaining} horas en NOW. */
    private static Ticket ticketWithRemaining(Long id, Priority priority, long remainingHours) {
        Instant deadline = TestData.NOW.plus(Duration.ofHours(remainingHours));
        Instant created = deadline.minus(Duration.ofHours(10));
        return new Ticket(id, 1L, "Sin conexión VPN", "No conecta la VPN desde ayer", Category.RED, priority,
                "MEDELLIN", "LT-0231", TicketStatus.ASIGNADO, 5L, false, created, deadline, null, null, 0L);
    }

    @Test
    void escalaSoloLosTicketsDentroDelUmbralYDejaHistorialDelSistema() {
        Ticket nearBreach = ticketWithRemaining(1L, Priority.MEDIA, 1);  // le queda el 10 %
        Ticket onTheEdge = ticketWithRemaining(2L, Priority.ALTA, 2);    // le queda justo el 20 %
        Ticket relaxed = ticketWithRemaining(3L, Priority.BAJA, 5);      // le queda el 50 %
        when(ticketRepository.findActiveNotEscalated()).thenReturn(List.of(nearBreach, onTheEdge, relaxed));

        int escalated = service.escalateNearBreach();

        assertThat(escalated).isEqualTo(2);
        assertThat(nearBreach.getPriority()).isEqualTo(Priority.ALTA);
        assertThat(nearBreach.isEscalated()).isTrue();
        assertThat(onTheEdge.getPriority()).isEqualTo(Priority.CRITICA);
        assertThat(relaxed.getPriority()).isEqualTo(Priority.BAJA);
        assertThat(relaxed.isEscalated()).isFalse();
        verify(ticketRepository, never()).save(relaxed);
        verify(metrics).ticketsEscalated(2);

        ArgumentCaptor<TicketHistory> history = ArgumentCaptor.forClass(TicketHistory.class);
        verify(historyRepository, org.mockito.Mockito.times(2)).save(history.capture());
        TicketHistory first = history.getAllValues().get(0);
        assertThat(first.action()).isEqualTo(HistoryAction.ESCALATED);
        assertThat(first.observation()).isEqualTo("Escalado por SLA: MEDIA -> ALTA");
        assertThat(first.performedBy()).isNull(); // lo hace el sistema
        assertThat(first.fromStatus()).isEqualTo(first.toStatus());
    }

    @Test
    void unTicketVencidoTambienSeEscalaYLaFechaLimiteNoCambia() {
        Ticket overdue = ticketWithRemaining(1L, Priority.CRITICA, -3);
        Instant deadline = overdue.getSlaDeadline();
        when(ticketRepository.findActiveNotEscalated()).thenReturn(List.of(overdue));

        assertThat(service.escalateNearBreach()).isEqualTo(1);
        assertThat(overdue.getPriority()).isEqualTo(Priority.CRITICA); // CRITICA ya es la máxima
        assertThat(overdue.isEscalated()).isTrue();
        assertThat(overdue.getSlaDeadline()).isEqualTo(deadline);
    }

    @Test
    void sinCandidatosReportaCero() {
        when(ticketRepository.findActiveNotEscalated()).thenReturn(List.of());

        assertThat(service.escalateNearBreach()).isZero();
        verify(ticketRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
        verify(metrics).ticketsEscalated(0);
    }
}
