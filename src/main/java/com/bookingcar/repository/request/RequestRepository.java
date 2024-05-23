package com.bookingcar.repository.request;

import com.bookingcar.entity.Request;
import com.bookingcar.entity.enums.RequestStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RequestRepository extends
        JpaRepository<Request, Long>,
        FilterRequestRepository,
        QuerydslPredicateExecutor<Request> {

    @Query("""
            select r from Request r
            where r.car.id = :carId
            and r.dateTimeFrom <= :dateTimeTo
            and r.dateTimeTo >= :dateTimeFrom
            and r.status in (:statuses)
            """)
    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<Request> findActiveRequestByCarId(Long carId,
                                               Instant dateTimeFrom,
                                               Instant dateTimeTo,
                                               List<RequestStatus> statuses);
    @Query("select r.id from Request r where r.dateTimeTo <= :currentTime and r.status = :status")
    List<Long> findFinishedRequests(Instant currentTime, RequestStatus status);

    @Modifying
    @Query("update Request r set r.status = :status where r.id in (:requestIds)")
    Integer updateStatus(List<Long> requestIds, RequestStatus status);
}
