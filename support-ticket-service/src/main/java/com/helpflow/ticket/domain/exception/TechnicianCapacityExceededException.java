package com.helpflow.ticket.domain.exception;

/** 409: el técnico ya tiene su carga máxima de tickets activos. */
public class TechnicianCapacityExceededException extends DomainException {

    public TechnicianCapacityExceededException(String technicianCode, int max) {
        super("El técnico " + technicianCode + " ya tiene su carga máxima de " + max + " tickets activos");
    }
}
