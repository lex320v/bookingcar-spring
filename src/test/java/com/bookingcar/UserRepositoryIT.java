package com.bookingcar;

import com.bookingcar.dto.user.UserFilterDto;
import com.bookingcar.entity.User;
import com.bookingcar.entity.enums.Gender;
import com.bookingcar.entity.enums.Role;
import com.bookingcar.entity.enums.UserStatus;
import com.bookingcar.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RequiredArgsConstructor
class UserRepositoryIT extends BaseIntegrationTest {

    private final UserRepository userRepository;

    @Test
    void findAllByFilterQueryDsl() {
        var user1 = buildUser("aaa", "Александр", "Кузьмин");
        var user2 = buildUser("bbb", "Алексей", "Кузьмиченко");
        var user3 = buildUser("ссс", "Андрей", "Кузьмин");

        userRepository.saveAll(List.of(user1, user2, user3));

        var filter = UserFilterDto.builder().firstname("лекс").build();
        var users = userRepository.findAllByFilterQueryDsl(filter);

        assertEquals(2, users.size());
    }

    @Test
    void findAllByFilterCriteria() {
        var user1 = buildUser("aaa", "Александр", "Кузьмин");
        var user2 = buildUser("bbb", "Алексей", "Кузьмиченко");
        var user3 = buildUser("ссс", "Андрей", "Кузьмин");

        userRepository.saveAll(List.of(user1, user2, user3));

        var filter = UserFilterDto.builder().firstname("лекс").build();
        var users = userRepository.findAllByFilterCriteria(filter);

        assertEquals(2, users.size());
    }

    @Test
    void findAllByFirstnameAndLastname() {
        var user1 = buildUser("aaa", "Александр", "Кузьмин");
        var user2 = buildUser("bbb", "Алексей", "Кузьмиченко");
        var user3 = buildUser("ссс", "Андрей", "Кузьмин");

        userRepository.saveAll(List.of(user1, user2, user3));

        var searchFirstname = "алекс";
        var searchLastname = "куз";
        var result = userRepository.findAllBy(searchFirstname, searchLastname);

        assertThat(result.size()).isEqualTo(2);

        var result1 = result.get(0);
        var result2 = result.get(1);

        assertTrue(result1.getFirstname().toLowerCase().contains(searchFirstname));
        assertTrue(result1.getLastname().toLowerCase().contains(searchLastname));

        assertTrue(result2.getFirstname().toLowerCase().contains(searchFirstname));
        assertTrue(result2.getLastname().toLowerCase().contains(searchLastname));
    }

    private User buildUser(String username, String firstname, String lastname) {
        return User.builder()
                .username(username)
                .firstname(firstname)
                .lastname(lastname)
                .password("qwerty")
                .status(UserStatus.ACTIVE)
                .gender(Gender.MALE)
                .role(Role.SUPER_ADMIN)
                .birthDate(LocalDate.of(2001, 1, 1))
                .build();
    }
}
