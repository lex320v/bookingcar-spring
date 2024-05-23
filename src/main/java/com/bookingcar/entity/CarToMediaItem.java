package com.bookingcar.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@ToString(exclude = {"car", "mediaItem"})
@EqualsAndHashCode(exclude = {"car", "mediaItem"})
public class CarToMediaItem implements IdentifiableEntity<CarToMediaItemId> {

    @EmbeddedId
    private CarToMediaItemId id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", insertable = false, updatable = false)
    private Car car;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "media_item_id", insertable = false, updatable = false)
    private MediaItem mediaItem;

    @Column(nullable = false)
    private Integer position;
}
