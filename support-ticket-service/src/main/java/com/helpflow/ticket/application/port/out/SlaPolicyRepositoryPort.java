package com.helpflow.ticket.application.port.out;

import com.helpflow.ticket.domain.model.SlaPolicy;
import com.helpflow.ticket.domain.model.enums.Priority;

import java.util.List;
import java.util.Optional;

public interface SlaPolicyRepositoryPort {

    Optional<SlaPolicy> findByPriority(Priority priority);

    List<SlaPolicy> findAll();

    SlaPolicy save(SlaPolicy policy);
}
