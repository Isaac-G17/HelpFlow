package com.helpflow.dispatch.service;

import com.helpflow.dispatch.dto.DispatchRequest;
import com.helpflow.dispatch.dto.DispatchResponse;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

/**
 * Simula la disponibilidad de técnicos de forma DETERMINISTA: la misma combinación
 * ticketId + categoría + prioridad produce siempre la misma respuesta (sin azar ni estado).
 */
@Service
public class TechnicianDispatchService {

    /** Catálogo fijo: 2 técnicos por categoría. Coincide con los técnicos semilla del servicio principal. */
    private static final Map<String, List<String>> CATALOG = Map.of(
            "HARDWARE", List.of("TEC-001", "TEC-002"),
            "SOFTWARE", List.of("TEC-003", "TEC-004"),
            "RED", List.of("TEC-005", "TEC-006"),
            "ACCESOS", List.of("TEC-007", "TEC-008"),
            "OTRO", List.of("TEC-009", "TEC-010")
    );

    private static final int MAX_HOURS = 12;

    public DispatchResponse dispatch(DispatchRequest request) {
        long seed = seed(request.ticketId(), request.categoria(), request.prioridad());

        List<String> candidates = CATALOG.get(request.categoria());
        String technician = candidates.get((int) Math.floorMod(seed, (long) candidates.size()));
        int hours = (int) Math.floorMod(seed >>> 8, (long) MAX_HOURS + 1);

        return new DispatchResponse(request.ticketId(), technician, classify(hours), hours);
    }

    /** Convierte la combinación en un número estable (CRC32 no depende de la JVM ni del arranque). */
    static long seed(Long ticketId, String categoria, String prioridad) {
        CRC32 crc = new CRC32();
        crc.update((ticketId + ":" + categoria + ":" + prioridad).getBytes(StandardCharsets.UTF_8));
        return crc.getValue();
    }

    static String classify(int hours) {
        if (hours <= 2) {
            return "ALTA";
        }
        if (hours <= 8) {
            return "MEDIA";
        }
        return "BAJA";
    }
}
