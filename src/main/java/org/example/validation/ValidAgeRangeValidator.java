package org.example.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.model.UserUpdateRequest;

public class ValidAgeRangeValidator implements ConstraintValidator<ValidAgeRange, UserUpdateRequest> {

    @Override
    public boolean isValid(UserUpdateRequest request, ConstraintValidatorContext context) {
        if (request == null) return true;
        Integer minAge = request.getMinAge();
        Integer maxAge = request.getMaxAge();
        if (minAge == null || maxAge == null) return true;
        if (minAge <= maxAge) return true;

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("minAge must not be greater than maxAge")
                .addPropertyNode("minAge")
                .addConstraintViolation();
        return false;
    }
}