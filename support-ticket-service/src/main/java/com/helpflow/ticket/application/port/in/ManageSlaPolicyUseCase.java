package com.helpflow.ticket.application.port.in;

import com.helpflow.ticket.domain.model.Priority;
import com.helpflow.ticket.domain.model.SlaPolicy;

import java.util.List;

public interface ManageSlaPolicyUseCase {

    List<SlaPolicy> list();

    SlaPolicy update(UpdateSlaPolicyCommand command);

    record UpdateSlaPolicyCommand(Priority priority, int responseHours, int resolutionHours) {
    }
}
