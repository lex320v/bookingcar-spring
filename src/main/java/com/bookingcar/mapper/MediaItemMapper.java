package com.bookingcar.mapper;

import com.bookingcar.dto.mediaItem.MediaItemReadDto;
import com.bookingcar.entity.MediaItem;
import org.mapstruct.Mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper( componentModel = SPRING)
public interface MediaItemMapper {

    MediaItemReadDto mediaItemToReadDto(MediaItem mediaItem);
}
