package com.helpflow.ticket.domain.exception;

/** 422: un usuario o técnico INACTIVO intenta operar. */
public class InactiveUserException extends BusinessRuleException {

    public InactiveUserException(String message) {
        super(message);
    }
}
