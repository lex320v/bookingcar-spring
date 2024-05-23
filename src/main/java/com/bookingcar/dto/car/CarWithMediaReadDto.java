package com.bookingcar.dto.car;

import com.bookingcar.dto.user.UserReadDto;
import com.bookingcar.entity.enums.CarType;
import lombok.Value;

import java.util.List;

@Value
public class CarWithMediaReadDto {
    Long id;
    String manufacturer;
    String model;
    Integer year;
    Integer horsepower;
    Integer price;
    Boolean active;
    CarType type;
    UserReadDto owner;
    List<CarToMediaItemReadDto> carToMediaItems;
}
