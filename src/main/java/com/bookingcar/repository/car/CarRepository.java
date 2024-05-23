package com.bookingcar.repository.car;

import com.bookingcar.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long>, FilterCarRepository {

    @Query("""
            select c from Car c
            join fetch c.owner o
            left join fetch o.avatarMediaItem ami
            left join fetch c.carToMediaItems ctmi
            left join fetch ctmi.mediaItem
            where c.id = :id
            order by ctmi.position
            """)
    Optional<Car> findByIdWithMediaAndOwner(Long id);
}
