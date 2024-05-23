package com.bookingcar.dto.car;

import com.bookingcar.dto.user.UserReadDto;
import com.bookingcar.entity.enums.CarType;
import lombok.Value;

import java.util.List;

@Value
public class CarReadDto {
    Long id;
    String manufacturer;
    String model;
    Integer year;
    Integer horsepower;
    Integer price;
    boolean active;
    CarType type;
    UserReadDto owner;
    List<CarToMediaItemReadDto> carToMediaItems;
}
