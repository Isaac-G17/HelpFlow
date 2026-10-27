package com.helpflow.dispatch.controller;

import com.helpflow.dispatch.dto.DispatchRequest;
import com.helpflow.dispatch.dto.DispatchResponse;
import com.helpflow.dispatch.service.TechnicianDispatchService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TechnicianDispatchController {

    private static final Logger log = LoggerFactory.getLogger(TechnicianDispatchController.class);

    private final TechnicianDispatchService service;

    public TechnicianDispatchController(TechnicianDispatchService service) {
        this.service = service;
    }

    @PostMapping("/technician-dispatch")
    public DispatchResponse dispatch(@Valid @RequestBody DispatchRequest request,
                                     @RequestHeader(value = "X-Trace-Id", required = false) String traceId) {
        DispatchResponse response = service.dispatch(request);
        log.info("dispatch traceId={} ticketId={} categoria={} prioridad={} -> tecnico={} horas={} disponibilidad={}",
                traceId, request.ticketId(), request.categoria(), request.prioridad(),
                response.tecnicoId(), response.tiempoEstimadoHoras(), response.disponibilidad());
        return response;
    }
}
