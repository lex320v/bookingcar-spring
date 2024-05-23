package com.bookingcar.mapper;

import com.bookingcar.dto.request.RequestCreateDto;
import com.bookingcar.dto.request.RequestReadDto;
import com.bookingcar.entity.Request;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, uses = {CarMapper.class, FeedbackMapper.class})
public interface RequestMapper {

    @Mapping(target = "dateTimeFrom", expression = "java(instantToLocalDateTime(request.getDateTimeFrom()))")
    @Mapping(target = "dateTimeTo", expression = "java(instantToLocalDateTime(request.getDateTimeTo()))")
    @Mapping(source = "request", target = "feedback")
    RequestReadDto requestToReadDto(Request request);

    Request createDtoToRequest(RequestCreateDto requestCreateDto);

    default LocalDateTime instantToLocalDateTime(Instant instant) {
        return instant.atZone(ZoneId.of("Europe/Moscow")).toLocalDateTime();
    }
}
