package com.bookingcar.validation.impl;

import com.bookingcar.entity.enums.RequestStatus;
import com.bookingcar.validation.RequestStatusValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RequestStatusValidatorImpl implements ConstraintValidator<RequestStatusValidator, RequestStatus> {

    private final Set<RequestStatus> prohibitedValues = new HashSet<>();

    @Override
    public void initialize(RequestStatusValidator constraintAnnotation) {
        prohibitedValues.addAll(Arrays.asList(constraintAnnotation.prohibitedValues()));
    }

    @Override
    public boolean isValid(RequestStatus value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        return !prohibitedValues.contains(value);
    }
}
