package com.bookingcar.repository.car;

import com.bookingcar.dto.PageDto;
import com.bookingcar.dto.UserPrincipalDto;
import com.bookingcar.dto.car.CarFilterDto;
import com.bookingcar.dto.car.CarReadWithStatsDto;
import com.bookingcar.dto.car.CarSortFields;
import com.bookingcar.entity.Car;
import com.bookingcar.entity.QRequest;
import com.bookingcar.entity.enums.RequestStatus;
import com.bookingcar.mapper.UserMapper;
import com.bookingcar.repository.CarToMediaItemRepository;
import com.bookingcar.repository.QPredicate;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilderFactory;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.Querydsl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.bookingcar.entity.QCar.car;
import static com.bookingcar.entity.QFeedback.feedback;
import static com.bookingcar.entity.QUser.user;
import static org.springframework.data.domain.Sort.Direction.ASC;

@RequiredArgsConstructor
public class FilterCarRepositoryImpl implements FilterCarRepository {

    private final EntityManager entityManager;
    private final UserMapper userMapper;
    private final CarToMediaItemRepository carToMediaItemRepository;

    @Override
    public PageImpl<CarReadWithStatsDto> findAllByFilter(CarFilterDto carFilterDto,
                                                         PageDto pageDto,
                                                         UserPrincipalDto userPrincipal) {
        var predicate = QPredicate.builder()
                .add(carFilterDto.getType(), car.type::eq)
                .add(carFilterDto.getMinYear(), car.year::goe)
                .add(carFilterDto.getMaxYear(), car.year::loe)
                .add(carFilterDto.getMinPrice(), car.price::goe)
                .add(carFilterDto.getMaxPrice(), car.price::loe)
                .add(carFilterDto.getMinHorsepower(), car.horsepower::goe)
                .add(carFilterDto.getMaxHorsepower(), car.horsepower::loe);

        if (carFilterDto.getSearch() != null) {
            predicate.addPredicate(
                    car.manufacturer.containsIgnoreCase(carFilterDto.getSearch())
                            .or(car.model.containsIgnoreCase(carFilterDto.getSearch()))
            );
        }

        if (carFilterDto.isMyCars()) {
            predicate.addPredicate(car.owner.id.eq(userPrincipal.getId()));
            predicate.add(carFilterDto.getActive(), car.active::eq);
        } else {
            predicate.addPredicate(car.active.eq(true));
        }

        var sumAlias = Expressions.numberPath(Integer.class, "sum");
        var countAlias = Expressions.numberPath(Long.class, "count");
        var avgAlias = Expressions.numberPath(Double.class, "avg");

        var isBookingStatusEnabled =
                carFilterDto.getDateTimeFrom() != null && carFilterDto.getDateTimeTo() != null;
        var requestDateFilter = new QRequest("dateFilter");
        var requestRating = new QRequest("rating");
        if (carFilterDto.getBookingStatus() != null) {
            switch (carFilterDto.getBookingStatus()) {
                case FREE -> predicate.addPredicate(requestDateFilter.car.id.isNull());
                case BOOKED -> predicate.addPredicate(requestDateFilter.car.id.isNotNull());
            }
        }

        Querydsl querydsl = new Querydsl(entityManager, (new PathBuilderFactory()).create(Car.class));
        var query = new JPAQuery<Car>(entityManager)
                .select(
                        car,
                        feedback.rating.sum().coalesce(0).as(sumAlias),
                        feedback.rating.count().as(countAlias),
                        Expressions.numberTemplate(
                                Double.class,
                                "round({0}, 2)",
                                feedback.rating.avg().coalesce(0.0)).as(avgAlias),
                        isBookingStatusEnabled ? requestDateFilter.car.id.isNull() : Expressions.nullExpression()
                )
                .from(car)
                .join(car.owner, user)
                .fetchJoin()
                .leftJoin(car.requests, requestRating)
                .leftJoin(requestRating.feedback, feedback)
                .groupBy(car.id, user.id)
                .where(predicate.buildAnd());

        if (isBookingStatusEnabled) {
            query.leftJoin(car.requests, requestDateFilter).on(
                    requestDateFilter.dateTimeFrom.loe(carFilterDto.getDateTimeTo())
                            .and(requestDateFilter.dateTimeTo.goe(carFilterDto.getDateTimeFrom()))
                            .and(requestDateFilter.status.in(RequestStatus.OPEN, RequestStatus.PROCESSING))
            );
            query.groupBy(requestDateFilter.car.id);
        }

        Sort sort;
        if (Objects.equals(pageDto.getSortField(), CarSortFields.avgRating.toString())
                || Objects.equals(pageDto.getSortField(), CarSortFields.sumRating.toString())) {

            switch (CarSortFields.valueOf(pageDto.getSortField())) {
                case avgRating -> query.orderBy(pageDto.getSortDirection() == ASC ? avgAlias.asc() : avgAlias.desc());
                case sumRating -> query.orderBy(pageDto.getSortDirection() == ASC ? sumAlias.asc() : sumAlias.desc());
            }
            sort = Sort.by(pageDto.getSortDirection(), CarSortFields.createdAt.toString());
        } else {
            sort = Sort.by(pageDto.getSortDirection(), pageDto.getSortField());
        }

        var pageable = PageRequest.of(pageDto.getPage(), pageDto.getSize(), sort);

        int totalElements = query.fetch().size();
        List<Tuple> result = querydsl.applyPagination(pageable, query).fetch();

        var carIds = result.stream().map(item -> item.get(car).getId()).toList();

        var carPreviewMap = carToMediaItemRepository.findAllByCarIds(carIds).stream()
                .collect(Collectors.toMap(
                        carToMediaItem -> carToMediaItem.getCar().getId(),
                        carToMediaItem -> carToMediaItem.getMediaItem().getId(),
                        (existing, replacement) -> existing
                ));

        var formattedResult = result.stream()
                .map(tuple -> {
                    var carEntity = tuple.get(car);
                    var sumRating = tuple.get(1, Integer.class);
                    var countRating = tuple.get(2, Long.class);
                    var avgRating = tuple.get(3, Double.class);
                    var free = isBookingStatusEnabled ? tuple.get(4, Boolean.class) : null;

                    return new CarReadWithStatsDto(
                            carEntity.getId(),
                            carEntity.getManufacturer(),
                            carEntity.getModel(),
                            carEntity.getYear(),
                            carEntity.getHorsepower(),
                            carEntity.getPrice(),
                            carEntity.isActive(),
                            carEntity.getType(),
                            userMapper.userToReadDto(carEntity.getOwner()),
                            carPreviewMap.getOrDefault(carEntity.getId(), null),
                            sumRating,
                            countRating,
                            avgRating,
                            free
                    );
                })
                .toList();

        return new PageImpl<>(formattedResult, pageable, totalElements);
    }
}
