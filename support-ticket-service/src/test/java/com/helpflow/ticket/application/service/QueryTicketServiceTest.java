package com.helpflow.ticket.application.service;

import com.helpflow.ticket.application.port.out.TechnicianRepositoryPort;
import com.helpflow.ticket.application.port.out.TicketHistoryRepositoryPort;
import com.helpflow.ticket.application.port.out.TicketRepositoryPort;
import com.helpflow.ticket.domain.exception.ForbiddenOperationException;
import com.helpflow.ticket.domain.exception.ResourceNotFoundException;
import com.helpflow.ticket.domain.model.CurrentUser;
import com.helpflow.ticket.domain.model.PageQuery;
import com.helpflow.ticket.domain.model.PageResult;
import com.helpflow.ticket.domain.model.TestData;
import com.helpflow.ticket.domain.model.Ticket;
import com.helpflow.ticket.domain.model.TicketFilter;
import com.helpflow.ticket.domain.model.TicketSummary;
import com.helpflow.ticket.domain.model.enums.Category;
import com.helpflow.ticket.domain.model.enums.Role;
import com.helpflow.ticket.domain.model.enums.TicketStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryTicketServiceTest {

    private static final CurrentUser ADMIN = new CurrentUser(99L, "admin@helpflow.com", Role.ADMIN);
    private static final CurrentUser ANA = new CurrentUser(1L, "ana@helpflow.com", Role.USUARIO);
    private static final CurrentUser LUIS = new CurrentUser(2L, "luis@helpflow.com", Role.USUARIO);
    private static final CurrentUser TEC05 = new CurrentUser(105L, "tec05@helpflow.com", Role.TECNICO);
    private static final CurrentUser TEC06 = new CurrentUser(106L, "tec06@helpflow.com", Role.TECNICO);
    private static final PageQuery PAGE = new PageQuery(0, 20);
    private static final TicketFilter NO_FILTER = new TicketFilter(null, null, TicketStatus.ABIERTO, null);

    @Mock
    private TicketRepositoryPort ticketRepository;
    @Mock
    private TicketHistoryRepositoryPort historyRepository;
    @Mock
    private TechnicianRepositoryPort technicianRepository;

    @InjectMocks
    private QueryTicketService service;

    /** Ticket de ana (requesterId = 1) asignado a TEC-005 (id 5). */
    private Ticket givenAnaTicket() {
        Ticket ticket = TestData.ticketInStatus(10L, TicketStatus.ASIGNADO, 5L);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        return ticket;
    }

    // ---------------- Propiedad (ensureCanView) ----------------

    @Test
    void elAdminVeCualquierTicket() {
        Ticket ticket = givenAnaTicket();

        assertThat(service.getById(10L, ADMIN)).isSameAs(ticket);
        verifyNoInteractions(technicianRepository);
    }

    @Test
    void elUsuarioVeSuPropioTicketPeroNoElDeOtro() {
        givenAnaTicket();

        assertThatCode(() -> service.getById(10L, ANA)).doesNotThrowAnyException();
        assertThatThrownBy(() -> service.getById(10L, LUIS))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("No tiene acceso a este ticket");
    }

    @Test
    void elTecnicoSoloVeLoQueTieneAsignado() {
        givenAnaTicket();
        when(technicianRepository.findByUserId(105L))
                .thenReturn(Optional.of(TestData.technician(5L, "TEC-005", Category.RED, "MEDELLIN", 1, 5)));
        when(technicianRepository.findByUserId(106L))
                .thenReturn(Optional.of(TestData.technician(6L, "TEC-006", Category.RED, "BOGOTA", 0, 5)));

        assertThatCode(() -> service.getById(10L, TEC05)).doesNotThrowAnyException();
        assertThatThrownBy(() -> service.getById(10L, TEC06)).isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void unTecnicoSinPerfilNoVeNada() {
        givenAnaTicket();
        when(technicianRepository.findByUserId(105L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(10L, TEC05)).isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void ticketInexistenteDevuelve404() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(10L, ADMIN)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void elHistorialAjenoNoSeConsulta() {
        givenAnaTicket();

        assertThatThrownBy(() -> service.history(10L, LUIS)).isInstanceOf(ForbiddenOperationException.class);
        verify(historyRepository, never()).findByTicketId(any());
    }

    // ---------------- Listado con filtro forzado por rol ----------------

    @Test
    void alUsuarioSeLeFuerzaElFiltroPorSolicitante() {
        TicketFilter forged = new TicketFilter(2L, null, null, null); // intenta ver los de luis
        when(ticketRepository.findAll(any(), eq(PAGE))).thenReturn(new PageResult<>(List.of(), 0, 20, 0));

        service.list(forged, PAGE, ANA);

        ArgumentCaptor<TicketFilter> filter = ArgumentCaptor.forClass(TicketFilter.class);
        verify(ticketRepository).findAll(filter.capture(), eq(PAGE));
        assertThat(filter.getValue().requesterId()).isEqualTo(1L);
    }

    @Test
    void alTecnicoSeLeFuerzaElFiltroPorSuPerfil() {
        when(technicianRepository.findByUserId(105L))
                .thenReturn(Optional.of(TestData.technician(5L, "TEC-005", Category.RED, "MEDELLIN", 1, 5)));
        when(ticketRepository.findAll(any(), eq(PAGE))).thenReturn(new PageResult<>(List.of(), 0, 20, 0));

        service.list(NO_FILTER, PAGE, TEC05);

        ArgumentCaptor<TicketFilter> filter = ArgumentCaptor.forClass(TicketFilter.class);
        verify(ticketRepository).findAll(filter.capture(), eq(PAGE));
        assertThat(filter.getValue().technicianId()).isEqualTo(5L);
        assertThat(filter.getValue().status()).isEqualTo(TicketStatus.ABIERTO);
    }

    @Test
    void unTecnicoSinPerfilRecibeUnaPaginaVacia() {
        when(technicianRepository.findByUserId(105L)).thenReturn(Optional.empty());

        PageResult<TicketSummary> result = service.list(NO_FILTER, PAGE, TEC05);

        assertThat(result.content()).isEmpty();
        verifyNoInteractions(ticketRepository);
    }

    @Test
    void elAdminListaConElFiltroTalCual() {
        when(ticketRepository.findAll(NO_FILTER, PAGE)).thenReturn(new PageResult<>(List.of(), 0, 20, 0));

        service.list(NO_FILTER, PAGE, ADMIN);

        verify(ticketRepository).findAll(NO_FILTER, PAGE);
    }
}
