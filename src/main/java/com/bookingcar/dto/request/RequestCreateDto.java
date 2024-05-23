package com.bookingcar.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.time.Instant;

@Value
@FieldNameConstants
public class RequestCreateDto {
    @NotNull
    Instant dateTimeFrom;

    @NotNull
    Instant dateTimeTo;

    @NotNull
    Long carId;
}
