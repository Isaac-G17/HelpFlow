package com.helpflow.ticket.infrastructure.adapter.in.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validación cruzada del cambio de estado (antes de llegar al dominio):
 *  - ASIGNADO no se pide por PATCH: solo se llega por asignación / reasignación.
 *  - CANCELADO exige una observación más completa (mínimo 15 caracteres) como justificación.
 * La validez de la transición según el estado actual la decide el dominio (409).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidStatusChangeValidator.class)
public @interface ValidStatusChange {

    String message() default "Cambio de estado inválido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
