package com.bookingcar.mapper;

import com.bookingcar.dto.user.UserCreateDto;
import com.bookingcar.dto.user.UserReadDto;
import com.bookingcar.dto.user.UserUpdateDto;
import com.bookingcar.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface UserMapper {

    @Mapping(source = "avatarMediaItem.id", target = "avatarMediaItemId")
    UserReadDto userToReadDto(User user);

    User updateDtoToUser(UserUpdateDto updateDto, @MappingTarget User user);

    User createDtoToUser(UserCreateDto createDto);
}
