package com.bookingcar.repository;

import com.bookingcar.entity.CarToMediaItem;
import com.bookingcar.entity.CarToMediaItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CarToMediaItemRepository extends JpaRepository<CarToMediaItem, CarToMediaItemId> {

    @Query("""
            select ctmi from CarToMediaItem ctmi
            join fetch ctmi.mediaItem mi
            where ctmi.car.id in (:carIds)
            order by ctmi.position
            """)
    List<CarToMediaItem> findAllByCarIds(List<Long> carIds);
}
