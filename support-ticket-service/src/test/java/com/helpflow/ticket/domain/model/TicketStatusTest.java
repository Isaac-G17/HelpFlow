package com.helpflow.ticket.domain.model;

import com.helpflow.ticket.domain.model.enums.Priority;
import com.helpflow.ticket.domain.model.enums.TicketStatus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class TicketStatusTest {

    @ParameterizedTest
    @CsvSource({
            "ABIERTO,ASIGNADO,true", "ABIERTO,CANCELADO,true", "ABIERTO,RESUELTO,false",
            "ASIGNADO,EN_PROGRESO,true", "ASIGNADO,CERRADO,false",
            "EN_PROGRESO,RESUELTO,true", "EN_PROGRESO,ABIERTO,false",
            "RESUELTO,CERRADO,true", "RESUELTO,EN_PROGRESO,true",
            "CERRADO,ABIERTO,false", "CANCELADO,ABIERTO,false", "ABIERTO,ABIERTO,false"
    })
    void maquinaDeEstados(TicketStatus from, TicketStatus to, boolean expected) {
        assertThat(from.canTransitionTo(to)).isEqualTo(expected);
    }

    @Test
    void laPrioridadSubeUnNivelYSeDetieneEnCritica() {
        assertThat(Priority.BAJA.next()).isEqualTo(Priority.MEDIA);
        assertThat(Priority.ALTA.next()).isEqualTo(Priority.CRITICA);
        assertThat(Priority.CRITICA.next()).isEqualTo(Priority.CRITICA);
    }
}
