package com.bookingcar.dto.request;

import com.bookingcar.dto.car.CarReadDto;
import com.bookingcar.dto.feedback.FeedbackReadDto;
import com.bookingcar.entity.enums.RequestStatus;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class RequestReadDto {
    Long id;
    LocalDateTime dateTimeFrom;
    LocalDateTime dateTimeTo;
    RequestStatus status;
    CarReadDto car;
    FeedbackReadDto feedback;
}
