package com.helpflow.ticket.application.service;

import com.helpflow.ticket.application.port.in.AssignTicketUseCase;
import com.helpflow.ticket.application.port.in.AssignTicketUseCase.AssignTicketCommand;
import com.helpflow.ticket.application.port.in.CreateTicketUseCase.CreateTicketCommand;
import com.helpflow.ticket.application.port.out.TechnicianDispatchPort;
import com.helpflow.ticket.domain.exception.TechnicianDispatchUnavailableException;
import com.helpflow.ticket.domain.model.Availability;
import com.helpflow.ticket.domain.model.Category;
import com.helpflow.ticket.domain.model.CurrentUser;
import com.helpflow.ticket.domain.model.DispatchSuggestion;
import com.helpflow.ticket.domain.model.Priority;
import com.helpflow.ticket.domain.model.Role;
import com.helpflow.ticket.domain.model.TestData;
import com.helpflow.ticket.domain.model.Ticket;
import com.helpflow.ticket.domain.model.TicketStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Prueba del flujo obligatorio con el TechnicianDispatchPort simulado (mock). */
@Disabled("TODO: quita esta línea cuando implementes CreateTicketService.create")
@ExtendWith(MockitoExtension.class)
class CreateTicketServiceTest {

    private static final CurrentUser ANA = new CurrentUser(1L, "ana@helpflow.com", Role.USUARIO);
    private static final CreateTicketCommand COMMAND = new CreateTicketCommand("Sin VPN",
            "No conecta la VPN corporativa", Category.RED, Priority.ALTA, "MEDELLIN", "LT-0231");

    @Mock
    private TicketRegistrationService registration;
    @Mock
    private TechnicianDispatchPort dispatchPort;
    @Mock
    private AssignTicketUseCase assignTicketUseCase;

    @InjectMocks
    private CreateTicketService service;

    @Test
    void consultaDispatchYAplicaLaAsignacion() {
        Ticket open = TestData.openTicket(10L, Category.RED, Priority.ALTA, "MEDELLIN");
        Ticket assigned = TestData.ticketInStatus(10L, TicketStatus.ASIGNADO, 5L);
        DispatchSuggestion suggestion = new DispatchSuggestion(10L, "TEC-005", Availability.MEDIA, 4);
        when(registration.open(COMMAND, ANA)).thenReturn(open);
        when(dispatchPort.requestTechnician(10L, Category.RED, Priority.ALTA)).thenReturn(suggestion);
        when(assignTicketUseCase.assign(new AssignTicketCommand(10L, suggestion, 1L))).thenReturn(assigned);

        Ticket result = service.create(COMMAND, ANA);

        assertThat(result.getStatus()).isEqualTo(TicketStatus.ASIGNADO);
    }

    @Test
    void siDispatchNoRespondeElTicketQuedaAbiertoYNoSePierde() {
        Ticket open = TestData.openTicket(10L, Category.RED, Priority.ALTA, "MEDELLIN");
        when(registration.open(COMMAND, ANA)).thenReturn(open);
        when(dispatchPort.requestTechnician(any(), any(), any()))
                .thenThrow(new TechnicianDispatchUnavailableException("caído", null));
        when(registration.reload(10L)).thenReturn(open);

        Ticket result = service.create(COMMAND, ANA);

        assertThat(result.getStatus()).isEqualTo(TicketStatus.ABIERTO);
        verify(registration).recordDispatchFailure(eq(10L), anyString());
        verify(assignTicketUseCase, never()).assign(any());
    }
}
