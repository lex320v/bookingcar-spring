package com.bookingcar.mapper;

import com.bookingcar.dto.car.CarToMediaItemReadDto;
import com.bookingcar.entity.CarToMediaItem;
import org.mapstruct.Mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, uses = {MediaItemMapper.class})
public interface CarToMediaItemMapper {

    CarToMediaItemReadDto carToMediaItemToReadDto(CarToMediaItem carToMediaItem);
}
