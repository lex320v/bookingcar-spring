package com.bookingcar.repository.request;

import com.bookingcar.dto.PageDto;
import com.bookingcar.dto.UserPrincipalDto;
import com.bookingcar.dto.request.RequestFilterDto;
import com.bookingcar.dto.request.RequestReadDto;
import com.bookingcar.entity.Request;
import com.bookingcar.entity.enums.Role;
import com.bookingcar.mapper.RequestMapper;
import com.bookingcar.repository.QPredicate;
import com.querydsl.core.types.dsl.PathBuilderFactory;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.Querydsl;

import static com.bookingcar.entity.QCar.car;
import static com.bookingcar.entity.QCarToMediaItem.carToMediaItem;
import static com.bookingcar.entity.QFeedback.feedback;
import static com.bookingcar.entity.QMediaItem.mediaItem;
import static com.bookingcar.entity.QRequest.request;

@RequiredArgsConstructor
public class FilterRequestRepositoryImpl implements FilterRequestRepository {

    private final EntityManager entityManager;
    private final RequestMapper requestMapper;

    @Override
    public PageImpl<RequestReadDto> findAllWithCar(RequestFilterDto requestFilterDto,
                                                   PageDto pageDto,
                                                   UserPrincipalDto userPrincipal) {
        var predicates = QPredicate.builder()
                .add(requestFilterDto.getStatus(), request.status::eq);

        if (userPrincipal.getAuthorities().contains(Role.OWNER)) {
            predicates.add(userPrincipal.getId(), request.car.owner.id::eq);
        } else {
            predicates.add(userPrincipal.getId(), request.client.id::eq);
        }

        var sort = Sort.by(pageDto.getSortDirection(), pageDto.getSortField());
        var pageable = PageRequest.of(pageDto.getPage(), pageDto.getSize(), sort);

        Querydsl querydsl = new Querydsl(entityManager, (new PathBuilderFactory()).create(Request.class));
        var query = new JPAQuery<Request>(entityManager)
                .select(request)
                .from(request)
                .join(request.car, car)
                .fetchJoin()
                .leftJoin(car.carToMediaItems, carToMediaItem)
                .fetchJoin()
                .leftJoin(carToMediaItem.mediaItem, mediaItem)
                .fetchJoin()
                .leftJoin(request.feedback, feedback)
                .fetchJoin()
                .where(predicates.buildAnd())
                .orderBy(carToMediaItem.position.asc());

        int totalElements = query.fetch().size();
        var result = querydsl.applyPagination(pageable, query).fetch();

        var formattedResult = result.stream().map(requestMapper::requestToReadDto).toList();

        return new PageImpl<>(formattedResult, pageable, totalElements);
    }
}
