package com.bookingcar.mapper;

import com.bookingcar.dto.car.CarCreateUpdateDto;
import com.bookingcar.dto.car.CarReadDto;
import com.bookingcar.entity.Car;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, uses = { UserMapper.class, CarToMediaItemMapper.class })
public interface CarMapper {

    CarReadDto carToReadDto(Car car);

    Car createDtoToCar(CarCreateUpdateDto carCreateDto);

    Car updateDtoToCar(CarCreateUpdateDto carUpdateDto, @MappingTarget Car car);
}
