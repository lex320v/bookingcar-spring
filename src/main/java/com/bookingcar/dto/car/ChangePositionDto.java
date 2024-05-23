package com.bookingcar.dto.car;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class ChangePositionDto {
    @NotNull
    Long mediaItemId;

    @NotNull
    Integer position;
}
