package com.bookingcar.dto.feedback;

import lombok.Value;

import java.time.Instant;

@Value
public class FeedbackReadDto {
    Long id;
    Integer rating;
    String text;
    Instant createdAt;
    Long authorId;
    String authorUsername;
}
