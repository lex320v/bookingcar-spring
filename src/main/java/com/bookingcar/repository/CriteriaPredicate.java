package com.bookingcar.repository;

import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CriteriaPredicate {

    private final List<Predicate> predicates = new ArrayList<>();

    public static CriteriaPredicate builder() {
        return new CriteriaPredicate();
    }

    public <T, U> CriteriaPredicate add(T search, U field, BiFunction<U, T, Predicate> function) {
        if (search != null) {
            predicates.add(function.apply(field, search));
        }

        return this;
    }

    public Predicate[] build() {
        return predicates.toArray(Predicate[]::new);
    }
}
