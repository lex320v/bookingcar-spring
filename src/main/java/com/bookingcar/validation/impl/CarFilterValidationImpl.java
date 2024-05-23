package com.bookingcar.validation.impl;

import com.bookingcar.dto.car.BookingStatus;
import com.bookingcar.dto.car.CarFilterDto;
import com.bookingcar.validation.CarFilterValidation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CarFilterValidationImpl implements ConstraintValidator<CarFilterValidation, CarFilterDto> {

    @Override
    public boolean isValid(CarFilterDto value, ConstraintValidatorContext context) {
        var bothDatesExists = value.getDateTimeFrom() != null && value.getDateTimeTo() != null;
        if (bothDatesExists &&
                (value.getDateTimeFrom().isAfter(value.getDateTimeTo()) ||
                        value.getDateTimeFrom().equals(value.getDateTimeTo()))) {
            var message = "dateTimeFrom: %s must be before than dateTimeTo: %s"
                    .formatted(value.getDateTimeFrom(), value.getDateTimeTo());
            setCustomMessage(context, message);

            return false;
        }

        if (value.getBookingStatus() != null &&
                value.getBookingStatus() != BookingStatus.ALL &&
                (value.getDateTimeFrom() == null || value.getDateTimeTo() == null)) {
            setCustomMessage(context, "for bookingStatus both dates required");

            return false;
        }

        if (value.getDateTimeFrom() != null && value.getDateTimeTo() == null) {
            setCustomMessage(context, "both dates required: dateTimeTo is missed");

            return false;
        }

        if (value.getDateTimeTo() != null && value.getDateTimeFrom() == null) {
            setCustomMessage(context, "both dates required: dateTimeFrom is missed");

            return false;
        }

        return true;
    }

    private void setCustomMessage(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}
