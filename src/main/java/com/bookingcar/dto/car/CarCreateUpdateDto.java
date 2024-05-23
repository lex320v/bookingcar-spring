package com.bookingcar.dto.car;

import com.bookingcar.entity.enums.CarType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import org.springframework.lang.Nullable;

@Value
@FieldNameConstants
public class CarCreateUpdateDto {
    @NotBlank
    @Size(min = 3, max = 20)
    String manufacturer;

    @NotBlank
    @Size(min = 2, max = 20)
    String model;

    @NotNull
    @Min(2000)
    @Max(2024)
    Integer year;

    @NotNull
    @Min(50)
    @Max(2000)
    Integer horsepower;

    @NotNull
    @Min(0)
    @Max(10_000_000)
    Integer price;

    @Nullable
    boolean active;

    @NotNull
    CarType type;
}
