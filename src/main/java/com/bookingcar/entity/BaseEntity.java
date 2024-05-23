package com.bookingcar.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.Instant;

@ToString
@EqualsAndHashCode
@Setter
@Getter
@MappedSuperclass
public abstract class BaseEntity<T extends Serializable> implements IdentifiableEntity<T> {

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
