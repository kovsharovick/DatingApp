package org.example.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidAgeRangeValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAgeRange {
    String message() default "minAge must not be greater than maxAge";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}