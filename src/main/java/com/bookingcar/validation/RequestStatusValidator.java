package com.bookingcar.validation;

import com.bookingcar.entity.enums.RequestStatus;
import com.bookingcar.validation.impl.RequestStatusValidatorImpl;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Constraint(validatedBy = RequestStatusValidatorImpl.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestStatusValidator {

    String message() default "Invalid or prohibited RequestStatus value";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    RequestStatus[] prohibitedValues();
}
