package com.helpflow.ticket.application.port.out;

import com.helpflow.ticket.domain.model.DispatchSuggestion;
import com.helpflow.ticket.domain.model.enums.Category;
import com.helpflow.ticket.domain.model.enums.Priority;

public interface TechnicianDispatchPort {

    /**
     * Consulta technician-dispatch-service.
     *
     * @throws com.helpflow.ticket.domain.exception.TechnicianDispatchUnavailableException si falla o no responde
     */
    DispatchSuggestion requestTechnician(Long ticketId, Category category, Priority priority);
}
