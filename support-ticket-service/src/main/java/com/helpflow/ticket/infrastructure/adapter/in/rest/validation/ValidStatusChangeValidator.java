package com.helpflow.ticket.infrastructure.adapter.in.rest.validation;

import com.helpflow.ticket.domain.model.TicketStatus;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.TicketDtos.ChangeStatusRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidStatusChangeValidator implements ConstraintValidator<ValidStatusChange, ChangeStatusRequest> {

    static final int CANCEL_MIN_OBSERVATION = 15;

    @Override
    public boolean isValid(ChangeStatusRequest request, ConstraintValidatorContext context) {
        if (request == null || request.status() == null) {
            return true; // @NotNull se encarga
        }
        if (request.status() == TicketStatus.ASIGNADO) {
            return violation(context, "status",
                    "ASIGNADO no se solicita por este endpoint; use la asignación automática o la reasignación");
        }
        if (request.status() == TicketStatus.CANCELADO
                && (request.observation() == null || request.observation().trim().length() < CANCEL_MIN_OBSERVATION)) {
            return violation(context, "observation",
                    "para cancelar, la observación debe tener al menos " + CANCEL_MIN_OBSERVATION + " caracteres");
        }
        return true;
    }

    private boolean violation(ConstraintValidatorContext context, String field, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addPropertyNode(field).addConstraintViolation();
        return false;
    }
}
