package com.bookingcar.repository.user;

import com.bookingcar.dto.PageDto;
import com.bookingcar.dto.user.UserFilterDto;
import com.bookingcar.entity.User;
import com.bookingcar.entity.User_;
import com.bookingcar.repository.CriteriaPredicate;
import com.bookingcar.repository.QPredicate;
import com.querydsl.core.types.dsl.PathBuilderFactory;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.Querydsl;

import java.util.List;

import static com.bookingcar.entity.QUser.user;

@RequiredArgsConstructor
public class FilterUserRepositoryImpl implements FilterUserRepository {

    private final EntityManager entityManager;

    @Override
    public List<User> findAllByFilterCriteria(UserFilterDto userFilterDto) {
        var cb = entityManager.getCriteriaBuilder();
        var criteria = cb.createQuery(User.class);
        var user = criteria.from(User.class);

        var predicates = CriteriaPredicate.builder()
                .add(userFilterDto.getFirstname(), user.get(User_.firstname), (a, b) -> cb.like(cb.upper(a), "%" + b.toUpperCase() + "%"))
                .add(userFilterDto.getLastname(), user.get(User_.lastname), (a, b) -> cb.like(cb.upper(a), "%" + b.toUpperCase() + "%"))
                .add(userFilterDto.getUsername(), user.get(User_.username), (a, b) -> cb.like(cb.upper(a), "%" + b.toUpperCase() + "%"))
                .build();

        criteria.select(user)
                .where(predicates)
                .orderBy(cb.asc(user.get(User_.createdAt)));

        return entityManager.createQuery(criteria).getResultList();
    }

    @Override
    public List<User> findAllByFilterQueryDsl(UserFilterDto userFilterDto) {
        var predicate = QPredicate.builder()
                .add(userFilterDto.getFirstname(), user.firstname::containsIgnoreCase)
                .add(userFilterDto.getLastname(), user.lastname::containsIgnoreCase)
                .add(userFilterDto.getUsername(), user.username::containsIgnoreCase)
                .buildAnd();

        return new JPAQuery<User>(entityManager)
                .select(user)
                .from(user)
                .where(predicate)
                .orderBy(user.createdAt.asc())
                .fetch();
    }

    @Override
    public PageImpl<User> findAllByFilterQueryDsl(UserFilterDto userFilterDto, PageDto pageDto) {
        var predicate = QPredicate.builder()
                .add(userFilterDto.getFirstname(), user.firstname::containsIgnoreCase)
                .add(userFilterDto.getLastname(), user.lastname::containsIgnoreCase)
                .add(userFilterDto.getUsername(), user.username::containsIgnoreCase)
                .add(userFilterDto.getRole(), user.role::eq)
                .buildAnd();

        Querydsl querydsl = new Querydsl(entityManager, (new PathBuilderFactory()).create(User.class));
        var query = new JPAQuery<User>(entityManager)
                .select(user)
                .from(user)
                .leftJoin(user.avatarMediaItem).fetchJoin()
                .where(predicate);

        Sort sort = Sort.by(pageDto.getSortDirection(), pageDto.getSortField());
        var pageable = PageRequest.of(pageDto.getPage(), pageDto.getSize(), sort);

        int totalElements = query.fetch().size();
        List<User> result = querydsl.applyPagination(pageable, query).fetch();

        return new PageImpl<>(result, pageable, totalElements);
    }
}
