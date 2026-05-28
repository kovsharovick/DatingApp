package org.example.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MinAgeValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)

public @interface MinAge {
    int value() default 18;

    String message() default "You must be at least {value} years old";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}