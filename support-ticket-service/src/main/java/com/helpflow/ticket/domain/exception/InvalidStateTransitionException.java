package com.helpflow.ticket.domain.exception;

import com.helpflow.ticket.domain.model.enums.TicketStatus;

/** 409: la máquina de estados no permite ese cambio. */
public class InvalidStateTransitionException extends DomainException {

    public InvalidStateTransitionException(TicketStatus from, TicketStatus to) {
        super("Transición no permitida: " + from + " -> " + to);
    }

    public InvalidStateTransitionException(String message) {
        super(message);
    }
}
