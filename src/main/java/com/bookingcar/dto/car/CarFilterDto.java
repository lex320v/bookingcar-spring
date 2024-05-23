package com.bookingcar.dto.car;

import com.bookingcar.entity.enums.CarType;
import com.bookingcar.validation.CarFilterValidation;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.Nullable;

import java.time.Instant;

@Value
@Builder
@CarFilterValidation
public class CarFilterDto {
    String search;
    CarType type;
    Integer minYear;
    Integer maxYear;
    Integer minPrice;
    Integer maxPrice;
    Integer minHorsepower;
    Integer maxHorsepower;
    BookingStatus bookingStatus;
    Instant dateTimeFrom;
    Instant dateTimeTo;
    Boolean active;

    @Nullable
    boolean myCars;
}
