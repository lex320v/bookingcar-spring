package com.bookingcar.repository;

import com.bookingcar.entity.MediaItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaItemRepository extends JpaRepository<MediaItem, Long> {

}
