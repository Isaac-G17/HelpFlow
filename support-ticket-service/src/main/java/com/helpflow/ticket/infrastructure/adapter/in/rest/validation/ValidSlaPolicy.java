package com.helpflow.ticket.infrastructure.adapter.in.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Validación cruzada de la configuración de SLA: el tiempo de resolución debe ser mayor que el de respuesta. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidSlaPolicyValidator.class)
public @interface ValidSlaPolicy {

    String message() default "resolutionHours debe ser mayor que responseHours";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
