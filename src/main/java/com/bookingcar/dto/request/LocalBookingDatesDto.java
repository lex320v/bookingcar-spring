package com.bookingcar.dto.request;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class LocalBookingDatesDto {

    LocalDateTime dateTimeFromLocal;
    LocalDateTime dateTimeToLocal;
}
