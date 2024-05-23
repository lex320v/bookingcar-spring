package com.bookingcar.repository.user;

import com.bookingcar.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, FilterUserRepository {

    @EntityGraph(attributePaths = {"avatarMediaItem"})
    @Query("""
                select u from users u
                where u.firstname ilike %:firstname% and u.lastname ilike %:lastname%
            """)
    List<User> findAllBy(String firstname, String lastname);

    Optional<User> findByUsername(String username);
}
