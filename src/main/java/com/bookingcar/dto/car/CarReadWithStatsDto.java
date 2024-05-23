package com.bookingcar.dto.car;

import com.bookingcar.dto.user.UserReadDto;
import com.bookingcar.entity.enums.CarType;
import lombok.Value;

@Value
public class CarReadWithStatsDto {
    Long id;
    String manufacturer;
    String model;
    Integer year;
    Integer horsepower;
    Integer price;
    Boolean active;
    CarType type;
    UserReadDto owner;
    Long previewMediaItemId;
    Integer sumRating;
    Long countRating;
    Double avgRating;
    Boolean free;
}
