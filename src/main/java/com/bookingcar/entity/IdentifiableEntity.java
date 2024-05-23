package com.bookingcar.entity;

import java.io.Serializable;

public interface IdentifiableEntity<T extends Serializable> {

    void setId(T id);

    T getId();
}
