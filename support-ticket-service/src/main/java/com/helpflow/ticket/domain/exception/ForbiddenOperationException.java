package com.helpflow.ticket.domain.exception;

/** 403: el recurso no es suyo o su rol no puede hacer esa operación sobre él. */
public class ForbiddenOperationException extends DomainException {

    public ForbiddenOperationException(String message) {
        super(message);
    }
}
