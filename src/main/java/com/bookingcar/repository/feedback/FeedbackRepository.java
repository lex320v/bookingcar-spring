package com.bookingcar.repository.feedback;

import com.bookingcar.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long>, FilterFeedbackRepository {

}
