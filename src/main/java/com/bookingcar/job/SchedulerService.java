package com.bookingcar.job;

import com.bookingcar.entity.enums.RequestStatus;
import com.bookingcar.repository.request.RequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final RequestRepository requestRepository;

    @Transactional
    @Scheduled(fixedDelay = 60000)
    public void finishRequests() {
        var outdatedRequestIds = requestRepository.findFinishedRequests(Instant.now(), RequestStatus.PROCESSING);
        if (!outdatedRequestIds.isEmpty()) {
            requestRepository.updateStatus(outdatedRequestIds, RequestStatus.FINISHED);
        }
    }
}
