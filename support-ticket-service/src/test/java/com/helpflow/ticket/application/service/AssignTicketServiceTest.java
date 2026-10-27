package com.helpflow.ticket.application.service;

import com.helpflow.ticket.application.port.in.AssignTicketUseCase.AssignTicketCommand;
import com.helpflow.ticket.application.port.out.AssignmentRepositoryPort;
import com.helpflow.ticket.application.port.out.TechnicianRepositoryPort;
import com.helpflow.ticket.application.port.out.TicketHistoryRepositoryPort;
import com.helpflow.ticket.application.port.out.TicketMetricsPort;
import com.helpflow.ticket.application.port.out.TicketRepositoryPort;
import com.helpflow.ticket.domain.model.Assignment;
import com.helpflow.ticket.domain.model.DispatchSuggestion;
import com.helpflow.ticket.domain.model.Technician;
import com.helpflow.ticket.domain.model.TestData;
import com.helpflow.ticket.domain.model.Ticket;
import com.helpflow.ticket.domain.model.TicketHistory;
import com.helpflow.ticket.domain.model.enums.AssignmentOrigin;
import com.helpflow.ticket.domain.model.enums.Availability;
import com.helpflow.ticket.domain.model.enums.Category;
import com.helpflow.ticket.domain.model.enums.HistoryAction;
import com.helpflow.ticket.domain.model.enums.Priority;
import com.helpflow.ticket.domain.model.enums.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AssignTicketServiceTest {

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

    private AssignTicketService service;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        service = new AssignTicketService(ticketRepository, technicianRepository, assignmentRepository,
                historyRepository, metrics, Clock.fixed(TestData.NOW, ZoneOffset.UTC));
        ticket = TestData.openTicket(10L, Category.RED, Priority.ALTA, "MEDELLIN"); // SLA: NOW + 8 h
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any())).then(returnsFirstArg());
        when(technicianRepository.save(any())).then(returnsFirstArg());
    }

    @Test
    void asignaAlTecnicoSugeridoSiCumpleTodasLasReglas() {
        Technician tec005 = TestData.technician(5L, "TEC-005", Category.RED, "MEDELLIN", 0, 5);
        when(technicianRepository.findByCode("TEC-005")).thenReturn(Optional.of(tec005));
        DispatchSuggestion suggestion = new DispatchSuggestion(10L, "TEC-005", Availability.MEDIA, 4);

        Ticket result = service.assign(new AssignTicketCommand(10L, suggestion, 1L));

        assertThat(result.getStatus()).isEqualTo(TicketStatus.ASIGNADO);
        assertThat(result.getTechnicianId()).isEqualTo(5L);
        assertThat(tec005.getActiveTickets()).isEqualTo(1);
        ArgumentCaptor<Assignment> assignment = ArgumentCaptor.forClass(Assignment.class);
        verify(assignmentRepository).save(assignment.capture());
        assertThat(assignment.getValue().getOrigin()).isEqualTo(AssignmentOrigin.DISPATCH);
        assertThat(assignment.getValue().getEstimatedHours()).isEqualTo(4);
        verify(metrics).ticketAssigned(AssignmentOrigin.DISPATCH);
    }

    @Test
    void usaUnTecnicoDeRespaldoSiElSugeridoEsDeOtraSede() {
        Technician tec006Bogota = TestData.technician(6L, "TEC-006", Category.RED, "BOGOTA", 0, 5);
        Technician tec005Medellin = TestData.technician(5L, "TEC-005", Category.RED, "MEDELLIN", 2, 5);
        when(technicianRepository.findByCode("TEC-006")).thenReturn(Optional.of(tec006Bogota));
        when(technicianRepository.findActiveCandidates(Category.RED)).thenReturn(List.of(tec006Bogota, tec005Medellin));

        Ticket result = service.assign(new AssignTicketCommand(10L,
                new DispatchSuggestion(10L, "TEC-006", Availability.ALTA, 1), 1L));

        assertThat(result.getTechnicianId()).isEqualTo(5L);
        verify(metrics).ticketAssigned(AssignmentOrigin.FALLBACK);
    }

    @Test
    void siNadieCumpleElTicketQuedaAbiertoConHistorial() {
        Technician tec005Full = TestData.technician(5L, "TEC-005", Category.RED, "MEDELLIN", 5, 5);
        when(technicianRepository.findByCode("TEC-005")).thenReturn(Optional.of(tec005Full));
        when(technicianRepository.findActiveCandidates(Category.RED)).thenReturn(List.of(tec005Full));

        Ticket result = service.assign(new AssignTicketCommand(10L,
                new DispatchSuggestion(10L, "TEC-005", Availability.BAJA, 12), 1L));

        assertThat(result.getStatus()).isEqualTo(TicketStatus.ABIERTO);
        verify(assignmentRepository, never()).save(any());
        ArgumentCaptor<TicketHistory> history = ArgumentCaptor.forClass(TicketHistory.class);
        verify(historyRepository).save(history.capture());
        assertThat(history.getValue().action()).isEqualTo(HistoryAction.DISPATCH_FAILED);
    }
}
