package com.helpflow.ticket.domain.exception;

/** Base de todas las excepciones de negocio. El dominio no sabe de HTTP: el GlobalExceptionHandler traduce cada tipo. */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
