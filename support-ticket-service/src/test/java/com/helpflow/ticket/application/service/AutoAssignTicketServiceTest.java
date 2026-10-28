package com.helpflow.ticket.application.service;

import com.helpflow.ticket.application.port.in.AssignTicketUseCase;
import com.helpflow.ticket.application.port.in.AssignTicketUseCase.AssignTicketCommand;
import com.helpflow.ticket.application.port.out.TechnicianDispatchPort;
import com.helpflow.ticket.application.port.out.TicketRepositoryPort;
import com.helpflow.ticket.domain.exception.InvalidStateTransitionException;
import com.helpflow.ticket.domain.exception.ResourceNotFoundException;
import com.helpflow.ticket.domain.exception.TechnicianDispatchUnavailableException;
import com.helpflow.ticket.domain.model.CurrentUser;
import com.helpflow.ticket.domain.model.DispatchSuggestion;
import com.helpflow.ticket.domain.model.TestData;
import com.helpflow.ticket.domain.model.Ticket;
import com.helpflow.ticket.domain.model.enums.Availability;
import com.helpflow.ticket.domain.model.enums.Category;
import com.helpflow.ticket.domain.model.enums.Priority;
import com.helpflow.ticket.domain.model.enums.Role;
import com.helpflow.ticket.domain.model.enums.TicketStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutoAssignTicketServiceTest {

    private static final CurrentUser ADMIN = new CurrentUser(99L, "admin@helpflow.com", Role.ADMIN);

    @Mock
    private TicketRepositoryPort ticketRepository;
    @Mock
    private TechnicianDispatchPort dispatchPort;
    @Mock
    private AssignTicketUseCase assignTicketUseCase;

    @InjectMocks
    private AutoAssignTicketService service;

    @Test
    void consultaDispatchYDelegaLaAsignacionConElAdminComoAutor() {
        Ticket open = TestData.openTicket(10L, Category.RED, Priority.ALTA, "MEDELLIN");
        Ticket assigned = TestData.ticketInStatus(10L, TicketStatus.ASIGNADO, 5L);
        DispatchSuggestion suggestion = new DispatchSuggestion(10L, "TEC-005", Availability.ALTA, 2);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(open));
        when(dispatchPort.requestTechnician(10L, Category.RED, Priority.ALTA)).thenReturn(suggestion);
        when(assignTicketUseCase.assign(new AssignTicketCommand(10L, suggestion, 99L))).thenReturn(assigned);

        Ticket result = service.autoAssign(10L, ADMIN);

        assertThat(result.getStatus()).isEqualTo(TicketStatus.ASIGNADO);
        assertThat(result.getTechnicianId()).isEqualTo(5L);
    }

    @Test
    void ticketInexistenteDevuelve404SinLlamarADispatch() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.autoAssign(10L, ADMIN))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(dispatchPort, assignTicketUseCase);
    }

    @Test
    void soloSeAutoAsignanTicketsAbiertos() {
        when(ticketRepository.findById(10L))
                .thenReturn(Optional.of(TestData.ticketInStatus(10L, TicketStatus.ASIGNADO, 5L)));

        assertThatThrownBy(() -> service.autoAssign(10L, ADMIN))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("ABIERTO");
        verifyNoInteractions(dispatchPort, assignTicketUseCase);
    }

    @Test
    void laFallaDeDispatchSePropagaParaResponder503() {
        when(ticketRepository.findById(10L))
                .thenReturn(Optional.of(TestData.openTicket(10L, Category.RED, Priority.ALTA, "MEDELLIN")));
        when(dispatchPort.requestTechnician(any(), any(), any()))
                .thenThrow(new TechnicianDispatchUnavailableException("caído", null));

        assertThatThrownBy(() -> service.autoAssign(10L, ADMIN))
                .isInstanceOf(TechnicianDispatchUnavailableException.class);
        verify(assignTicketUseCase, never()).assign(any());
    }
}
