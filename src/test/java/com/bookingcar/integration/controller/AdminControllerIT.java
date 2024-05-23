package com.bookingcar.integration.controller;

import com.bookingcar.BaseIntegrationTest;
import com.bookingcar.dto.PageDto;
import com.bookingcar.dto.PageResponse;
import com.bookingcar.dto.user.UserCreateDto;
import com.bookingcar.dto.user.UserFilterDto;
import com.bookingcar.entity.User;
import com.bookingcar.entity.enums.Gender;
import com.bookingcar.entity.enums.Role;
import com.bookingcar.entity.enums.UserStatus;
import com.bookingcar.integration.SecurityContextHelper;
import com.bookingcar.repository.user.UserRepository;
import com.bookingcar.service.UserService;
import com.bookingcar.util.TestDataImporter;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static com.bookingcar.dto.user.UserCreateDto.Fields.birthDate;
import static com.bookingcar.dto.user.UserCreateDto.Fields.firstname;
import static com.bookingcar.dto.user.UserCreateDto.Fields.gender;
import static com.bookingcar.dto.user.UserCreateDto.Fields.lastname;
import static com.bookingcar.dto.user.UserCreateDto.Fields.password;
import static com.bookingcar.dto.user.UserCreateDto.Fields.role;
import static com.bookingcar.dto.user.UserCreateDto.Fields.username;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@AutoConfigureMockMvc
@RequiredArgsConstructor
public class AdminControllerIT extends BaseIntegrationTest {

    private final MockMvc mockMvc;
    private final UserRepository userRepository;
    private final EntityManager entityManager;
    private final UserService userService;
    private final SecurityContextHelper securityContextHelper;

    @BeforeEach
    void init() {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContextHelper.authenticateUser(Role.ADMIN, securityContext);
    }

    @Test
    void createAdminView() throws Exception {
        mockMvc.perform(get("/admins/create"))
                .andExpectAll(
                        status().is2xxSuccessful(),
                        view().name("/admin/create")
                );
    }

    @Test
    void findUsers() throws Exception {
        TestDataImporter.importData(entityManager);

        var filter = UserFilterDto.builder().build();
        var pageDto = new PageDto();

        var result = PageResponse.of(userService.findAll(filter, pageDto));

        mockMvc.perform(get("/admins/users"))
                .andExpectAll(
                        status().is2xxSuccessful(),
                        view().name("admin/users"),
                        model().attributeExists("users"),
                        model().attribute("users", result)
                );
    }

    @Test
    void blockUser() throws Exception {
        var user = User.builder()
                .username("test")
                .firstname("test")
                .lastname("test")
                .password("test")
                .role(Role.CLIENT)
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(2001, 1, 1))
                .status(UserStatus.ACTIVE)
                .build();

        var savedUser = userRepository.save(user);

        mockMvc.perform(
                        post("/admins/users/edit-status/" + savedUser.getId())
                                .param("status", UserStatus.INACTIVE.toString())
                                .with(csrf())
                )
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/admins/users")
                );

        var blockedUser = userRepository.getReferenceById(savedUser.getId());

        assertThat(blockedUser.getStatus()).isEqualTo(UserStatus.INACTIVE);
    }

    @Test
    void unblockUser() throws Exception {
        var user = User.builder()
                .username("test")
                .firstname("test")
                .lastname("test")
                .password("test")
                .role(Role.CLIENT)
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(2001, 1, 1))
                .status(UserStatus.INACTIVE)
                .build();

        var savedUser = userRepository.save(user);

        mockMvc.perform(
                        post("/admins/users/edit-status/" + savedUser.getId())
                                .param("status", UserStatus.ACTIVE.toString())
                                .with(csrf())
                )
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/admins/users")
                );

        var blockedUser = userRepository.getReferenceById(savedUser.getId());

        assertThat(blockedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void createAdmin() throws Exception {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContextHelper.authenticateUser(Role.SUPER_ADMIN, securityContext);

        var userCreateDto = new UserCreateDto(
                "test",
                "test",
                "test",
                "test",
                Role.ADMIN,
                Gender.MALE,
                LocalDate.of(2001, 1, 1)
        );

        mockMvc.perform(
                        post("/admins")
                                .param(username, userCreateDto.getUsername())
                                .param(password, userCreateDto.getPassword())
                                .param(firstname, userCreateDto.getFirstname())
                                .param(lastname, userCreateDto.getLastname())
                                .param(role, userCreateDto.getRole().toString())
                                .param(gender, userCreateDto.getGender().toString())
                                .param(birthDate, userCreateDto.getBirthDate().toString())
                                .with(csrf())
                )
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/admins/users")
                )
                .andReturn();

        var createdUser = userRepository.findByUsername(userCreateDto.getUsername());

        createdUser.ifPresentOrElse((user) -> {
                    assertThat(user.getUsername()).isEqualTo(userCreateDto.getUsername());
                    assertThat(user.getFirstname()).isEqualTo(userCreateDto.getFirstname());
                    assertThat(user.getLastname()).isEqualTo(userCreateDto.getLastname());
                    assertThat(user.getRole()).isEqualTo(userCreateDto.getRole());
                    assertThat(user.getGender()).isEqualTo(userCreateDto.getGender());
                    assertThat(user.getBirthDate()).isEqualTo(userCreateDto.getBirthDate());
                },
                Assertions::fail);
    }
}
