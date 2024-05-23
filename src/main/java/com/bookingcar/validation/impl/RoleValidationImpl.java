package com.bookingcar.validation.impl;

import com.bookingcar.entity.enums.Role;
import com.bookingcar.validation.RoleValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RoleValidationImpl implements ConstraintValidator<RoleValidator, Role> {

    private final Set<Role> allowedValues = new HashSet<>();

    @Override
    public void initialize(RoleValidator constraintAnnotation) {
        allowedValues.addAll(Arrays.asList(constraintAnnotation.allowedValues()));
    }

    @Override
    public boolean isValid(Role value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        return allowedValues.contains(value);
    }

}
