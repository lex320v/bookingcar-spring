package com.bookingcar.repository.feedback;

import com.bookingcar.dto.PageDto;
import com.bookingcar.dto.feedback.FeedbackReadDto;
import org.springframework.data.domain.PageImpl;

public interface FilterFeedbackRepository {

    PageImpl<FeedbackReadDto> findAll(Long carId, PageDto pageDto);
}
