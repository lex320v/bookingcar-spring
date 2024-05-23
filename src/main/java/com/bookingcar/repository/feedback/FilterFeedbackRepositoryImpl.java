package com.bookingcar.repository.feedback;

import com.bookingcar.dto.PageDto;
import com.bookingcar.dto.feedback.FeedbackReadDto;
import com.bookingcar.entity.Feedback;
import com.bookingcar.entity.Request;
import com.bookingcar.mapper.FeedbackMapper;
import com.querydsl.core.types.dsl.PathBuilderFactory;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.Querydsl;

import static com.bookingcar.entity.QFeedback.feedback;
import static com.bookingcar.entity.QRequest.request;
import static com.bookingcar.entity.QUser.user;

@RequiredArgsConstructor
public class FilterFeedbackRepositoryImpl implements FilterFeedbackRepository {

    private final EntityManager entityManager;
    private final FeedbackMapper feedbackMapper;

    @Override
    public PageImpl<FeedbackReadDto> findAll(Long carId, PageDto pageDto) {
        var sort = Sort.by(pageDto.getSortDirection(), pageDto.getSortField());
        var pageable = PageRequest.of(pageDto.getPage(), pageDto.getSize(), sort);

        Querydsl querydsl = new Querydsl(entityManager, (new PathBuilderFactory()).create(Request.class));
        var query = new JPAQuery<Feedback>(entityManager)
                .select(request)
                .from(request)
                .join(request.feedback, feedback).fetchJoin()
                .join(request.client, user).fetchJoin()
                .where(request.car.id.eq(carId))
                .orderBy(feedback.createdAt.desc());

        int totalElements = query.fetch().size();
        var result = querydsl.applyPagination(pageable, query).fetch();

        var formattedResult = result.stream()
                .map(feedbackMapper::feedbackToReadDto)
                .toList();

        return new PageImpl<>(formattedResult, pageable, totalElements);
    }
}
