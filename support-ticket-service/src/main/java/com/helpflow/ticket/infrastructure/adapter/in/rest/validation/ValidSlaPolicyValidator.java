package com.helpflow.ticket.infrastructure.adapter.in.rest.validation;

import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.SlaPolicyDtos.SlaPolicyRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidSlaPolicyValidator implements ConstraintValidator<ValidSlaPolicy, SlaPolicyRequest> {

    @Override
    public boolean isValid(SlaPolicyRequest request, ConstraintValidatorContext context) {
        if (request == null || request.resolutionHours() > request.responseHours()) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("resolutionHours")
                .addConstraintViolation();
        return false;
    }
}
