package com.bookingcar.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;

@ToString(callSuper = true)
@Setter
@Getter
@MappedSuperclass
public abstract class BaseEntitySoftDelete<T extends Serializable> extends BaseEntity<T> implements IdentifiableEntity<T> {

    private Instant deletedAt;
}
