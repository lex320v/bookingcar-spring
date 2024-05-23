package com.bookingcar.validation;

import com.bookingcar.entity.enums.Role;
import com.bookingcar.validation.impl.RoleValidationImpl;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = RoleValidationImpl.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RoleValidators.class)
public @interface RoleValidator {

    String message() default "Invalid or prohibited Role value";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Role[] allowedValues();
}
