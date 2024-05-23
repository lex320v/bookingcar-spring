package com.bookingcar.mapper;

import com.bookingcar.dto.feedback.FeedbackCreateDto;
import com.bookingcar.dto.feedback.FeedbackReadDto;
import com.bookingcar.entity.Feedback;
import com.bookingcar.entity.Request;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface FeedbackMapper {

    @Mappings({
            @Mapping(source = "feedback.id", target = "id"),
            @Mapping(source = "feedback.rating", target = "rating"),
            @Mapping(source = "feedback.text", target = "text"),
            @Mapping(source = "feedback.createdAt", target = "createdAt"),
            @Mapping(source = "client.id", target = "authorId"),
            @Mapping(source = "client.username", target = "authorUsername")
    })
    FeedbackReadDto feedbackToReadDto(Request request);

    Feedback createDtoToFeedback(FeedbackCreateDto feedbackCreateDto);
}
