package com.bookingcar.service;

import com.bookingcar.dto.PageDto;
import com.bookingcar.dto.UserPrincipalDto;
import com.bookingcar.dto.feedback.FeedbackCreateDto;
import com.bookingcar.dto.feedback.FeedbackReadDto;
import com.bookingcar.entity.enums.RequestStatus;
import com.bookingcar.exceptions.AccessException;
import com.bookingcar.mapper.FeedbackMapper;
import com.bookingcar.repository.feedback.FeedbackRepository;
import com.bookingcar.repository.request.RequestRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final RequestRepository requestRepository;
    private final FeedbackMapper feedbackMapper;

    public PageImpl<FeedbackReadDto> findAllByCarId(Long carId, PageDto pageDto) {
        return feedbackRepository.findAll(carId, pageDto);
    }

    @Transactional
    public void createOrUpdate(FeedbackCreateDto feedbackCreateDto, UserPrincipalDto userPrincipal) {
        var request = requestRepository.findById(feedbackCreateDto.getRequestId())
                .orElseThrow(() -> new ValidationException("request with id: " + feedbackCreateDto + " does not exist"));
        if (!request.getClient().getId().equals(userPrincipal.getId())) {
            throw new AccessException("you are not creator of request with id: " + request.getId());
        }
        if (!request.getStatus().equals(RequestStatus.FINISHED)) {
            throw new ValidationException("requestId: " + feedbackCreateDto.getRequestId()
                    + " prohibited to leave feedback for request with status: " + request.getStatus());
        }

        var feedback = feedbackMapper.createDtoToFeedback(feedbackCreateDto);
        var existingFeedback = request.getFeedback();
        if (existingFeedback != null) {
            existingFeedback.setRating(feedbackCreateDto.getRating());
            existingFeedback.setText(feedbackCreateDto.getText());
            requestRepository.flush();

            return;
        }

        var savedFeedback = feedbackRepository.save(feedback);
        request.setFeedback(savedFeedback);
        requestRepository.saveAndFlush(request);
    }
}
