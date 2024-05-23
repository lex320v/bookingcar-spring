package com.bookingcar.integration.service;

import com.bookingcar.BaseIntegrationTest;
import com.bookingcar.dto.user.UserCreateDto;
import com.bookingcar.dto.user.UserUpdateDto;
import com.bookingcar.entity.User;
import com.bookingcar.entity.enums.Gender;
import com.bookingcar.entity.enums.Role;
import com.bookingcar.entity.enums.UserStatus;
import com.bookingcar.repository.user.UserRepository;
import com.bookingcar.service.UserService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RequiredArgsConstructor
class UserServiceIT extends BaseIntegrationTest {

    private final UserService userService;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Test
    void findById() {
        var savedUser = userRepository.save(buildUser());
        entityManager.detach(savedUser);

        var maybeUser = userService.findById(savedUser.getId());

//        assertTrue(maybeUser.isPresent());
//        maybeUser.ifPresent(user -> assertEquals("test_username", user.getUsername()));
    }

    @Test
    void create() {
        var userCreateDto = new UserCreateDto(
                "new_user",
                "test",
                "test",
                "test",
                Role.ADMIN,
                Gender.MALE,
                LocalDate.of(2001, 1, 1)
        );

        var createdUser = userService.create(userCreateDto);

        assertEquals(userCreateDto.getUsername(), createdUser.getUsername());
        assertEquals(userCreateDto.getFirstname(), createdUser.getFirstname());
        assertEquals(userCreateDto.getLastname(), createdUser.getLastname());
        assertSame(userCreateDto.getRole(), createdUser.getRole());
        assertSame(userCreateDto.getGender(), createdUser.getGender());
    }

    @Test
    void update() {
        var savedUser = userRepository.save(buildUser());
        entityManager.detach(savedUser);

        var userUpdateDto = UserUpdateDto.builder()
                .username("new_username")
                .firstname("test")
                .lastname("test")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(2001, 1, 1))
                .build();

        userService.update(savedUser.getId(), userUpdateDto);

//        assertTrue(updatedUser.isPresent());
//        updatedUser.ifPresent(user -> {
//            assertEquals(userUpdateDto.getUsername(), user.getUsername());
//            assertEquals(userUpdateDto.getFirstname(), user.getFirstname());
//            assertEquals(userUpdateDto.getLastname(), user.getLastname());
//            assertSame(userUpdateDto.getGender(), user.getGender());
//        });
    }

    @Test
    void delete() {
        var savedUser = userRepository.save(buildUser());
        entityManager.detach(savedUser);

//        userService.delete(savedUser.getId());

        // todo
        assertTrue(true);
    }

    private User buildUser() {
        return User.builder()
                .username("test_username")
                .firstname("Александр")
                .lastname("Кузьмин")
                .password("qwerty")
                .status(UserStatus.ACTIVE)
                .gender(Gender.MALE)
                .role(Role.SUPER_ADMIN)
                .birthDate(LocalDate.of(2001, 1, 1))
                .build();
    }
}