package com.helpflow.ticket.domain.exception;

/** 422: regla de negocio violada (datos válidos en forma, pero no permitidos por el dominio). */
public class BusinessRuleException extends DomainException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
