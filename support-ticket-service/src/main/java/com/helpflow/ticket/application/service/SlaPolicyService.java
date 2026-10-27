package com.helpflow.ticket.application.service;

import com.helpflow.ticket.application.port.in.ManageSlaPolicyUseCase;
import com.helpflow.ticket.application.port.out.SlaPolicyRepositoryPort;
import com.helpflow.ticket.domain.exception.ResourceNotFoundException;
import com.helpflow.ticket.domain.model.SlaPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SlaPolicyService implements ManageSlaPolicyUseCase {

    private static final Logger log = LoggerFactory.getLogger(SlaPolicyService.class);

    private final SlaPolicyRepositoryPort slaPolicyRepository;

    public SlaPolicyService(SlaPolicyRepositoryPort slaPolicyRepository) {
        this.slaPolicyRepository = slaPolicyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlaPolicy> list() {
        return slaPolicyRepository.findAll();
    }

    @Override
    @Transactional
    public SlaPolicy update(UpdateSlaPolicyCommand command) {
        SlaPolicy current = slaPolicyRepository.findByPriority(command.priority())
                .orElseThrow(() -> new ResourceNotFoundException("No existe política de SLA para " + command.priority()));
        SlaPolicy updated = slaPolicyRepository.save(current.withHours(command.responseHours(), command.resolutionHours()));
        log.info("SLA actualizado priority={} response={}h resolution={}h (aplica a tickets nuevos)",
                updated.priority(), updated.responseHours(), updated.resolutionHours());
        return updated;
    }
}
