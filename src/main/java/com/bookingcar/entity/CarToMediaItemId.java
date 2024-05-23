package com.bookingcar.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class CarToMediaItemId implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "car_id")
    private Long carId;

    @Column(name = "media_item_id")
    private Long mediaItemId;
}
