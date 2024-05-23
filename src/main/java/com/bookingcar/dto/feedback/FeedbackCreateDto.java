package com.bookingcar.dto.feedback;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

@Value
@Builder
@FieldNameConstants
public class FeedbackCreateDto {

    public static final int MAX_RATING = 10;
    public static final int MIN_RATING = 1;

    @NotNull
    @Positive
    Long requestId;

    @NotNull
    @Max(MAX_RATING)
    @Min(MIN_RATING)
    Integer rating;

    String text;
}
