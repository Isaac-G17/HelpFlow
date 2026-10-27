package com.helpflow.ticket.infrastructure.adapter.in.rest;

import com.helpflow.ticket.application.port.in.ManageTechnicianUseCase;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.TechnicianDtos.CreateTechnicianRequest;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.TechnicianDtos.TechnicianResponse;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.TechnicianDtos.UpdateTechnicianRequest;
import com.helpflow.ticket.infrastructure.adapter.in.rest.mapper.RestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/technicians")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Técnicos (ADMIN)")
public class TechnicianController {

    private final ManageTechnicianUseCase manageTechnicianUseCase;
    private final RestMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea el usuario TECNICO y su perfil de soporte")
    public TechnicianResponse create(@Valid @RequestBody CreateTechnicianRequest request) {
        return mapper.toResponse(manageTechnicianUseCase.create(mapper.toCommand(request)));
    }

    @GetMapping
    @Operation(summary = "Listar técnicos con su carga actual")
    public List<TechnicianResponse> list() {
        return manageTechnicianUseCase.list().stream().map(mapper::toResponse).toList();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar perfil de soporte (especialidad, sede, carga máxima, estado)")
    public TechnicianResponse update(@PathVariable Long id, @Valid @RequestBody UpdateTechnicianRequest request) {
        return mapper.toResponse(manageTechnicianUseCase.update(id, mapper.toCommand(request)));
    }
}
