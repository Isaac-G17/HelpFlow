package com.helpflow.ticket.domain.exception;

/** 503: technician-dispatch-service no respondió, tardó demasiado o devolvió un error. */
public class TechnicianDispatchUnavailableException extends DomainException {

    public TechnicianDispatchUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
