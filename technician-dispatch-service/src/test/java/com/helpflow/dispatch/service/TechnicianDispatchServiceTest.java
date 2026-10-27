package com.helpflow.dispatch.service;

import com.helpflow.dispatch.dto.DispatchRequest;
import com.helpflow.dispatch.dto.DispatchResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TechnicianDispatchServiceTest {

    private final TechnicianDispatchService service = new TechnicianDispatchService();

    @Test
    void mismaEntradaDevuelveSiempreLaMismaRespuesta() {
        DispatchRequest request = new DispatchRequest(125L, "RED", "ALTA");

        DispatchResponse first = service.dispatch(request);
        DispatchResponse second = service.dispatch(request);

        assertThat(second).isEqualTo(first);
    }

    @Test
    void elTecnicoSugeridoPerteneceALaCategoria() {
        for (long id = 1; id <= 50; id++) {
            DispatchResponse response = service.dispatch(new DispatchRequest(id, "RED", "MEDIA"));
            assertThat(response.tecnicoId()).isIn("TEC-005", "TEC-006", "TEC-013");
            assertThat(response.tiempoEstimadoHoras()).isBetween(0, 12);
        }
    }

    @Test
    void combinacionesDistintasProducenResultadosVariados() {
        Set<String> results = new HashSet<>();
        for (long id = 1; id <= 50; id++) {
            DispatchResponse r = service.dispatch(new DispatchRequest(id, "SOFTWARE", "BAJA"));
            results.add(r.tecnicoId() + "-" + r.tiempoEstimadoHoras());
        }
        assertThat(results).hasSizeGreaterThan(5);
    }

    @ParameterizedTest
    @CsvSource({"0,ALTA", "2,ALTA", "3,MEDIA", "8,MEDIA", "9,BAJA", "12,BAJA"})
    void clasificaLaDisponibilidadSegunLasHoras(int hours, String expected) {
        assertThat(TechnicianDispatchService.classify(hours)).isEqualTo(expected);
    }
}
