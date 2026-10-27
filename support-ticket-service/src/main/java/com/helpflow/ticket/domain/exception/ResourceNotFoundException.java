package com.helpflow.ticket.domain.exception;

/** 404 */
public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " con id " + id + " no existe");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
