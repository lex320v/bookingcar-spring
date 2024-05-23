package com.bookingcar.dto.request;

import com.bookingcar.entity.enums.RequestStatus;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RequestFilterDto {
    RequestStatus status;
}
