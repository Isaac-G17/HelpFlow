package com.helpflow.ticket.domain.exception;

/** 409: documento, correo o código ya registrado. */
public class DuplicateResourceException extends DomainException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
