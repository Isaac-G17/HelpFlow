package com.helpflow.ticket.domain.model;

import com.helpflow.ticket.domain.exception.BusinessRuleException;

import java.util.Locale;

/** Utilidades de validación del dominio (Java puro). */
final class DomainValidation {

    private DomainValidation() {
    }

    static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException("El campo '" + field + "' es obligatorio");
        }
        return value.trim();
    }

    static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new BusinessRuleException("El campo '" + field + "' es obligatorio");
        }
        return value;
    }

    /** Las sedes se guardan normalizadas (MEDELLIN, BOGOTA...) para poder compararlas. */
    static String normalizeSede(String sede) {
        return requireText(sede, "sede").toUpperCase(Locale.ROOT);
    }
}
