package com.bookingcar.integration.controller;

import com.bookingcar.BaseIntegrationTest;
import com.bookingcar.dto.user.UserCreateDto;
import com.bookingcar.dto.user.UserReadDto;
import com.bookingcar.dto.user.UserUpdateDto;
import com.bookingcar.entity.User;
import com.bookingcar.entity.enums.Gender;
import com.bookingcar.entity.enums.Role;
import com.bookingcar.entity.enums.UserStatus;
import com.bookingcar.integration.SecurityContextHelper;
import com.bookingcar.repository.user.UserRepository;
import com.bookingcar.service.UserService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static com.bookingcar.dto.user.ChangePasswordDto.Fields.newPassword;
import static com.bookingcar.dto.user.ChangePasswordDto.Fields.oldPassword;
import static com.bookingcar.dto.user.UserCreateDto.Fields.birthDate;
import static com.bookingcar.dto.user.UserCreateDto.Fields.firstname;
import static com.bookingcar.dto.user.UserCreateDto.Fields.gender;
import static com.bookingcar.dto.user.UserCreateDto.Fields.lastname;
import static com.bookingcar.dto.user.UserCreateDto.Fields.password;
import static com.bookingcar.dto.user.UserCreateDto.Fields.role;
import static com.bookingcar.dto.user.UserCreateDto.Fields.username;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@AutoConfigureMockMvc
@RequiredArgsConstructor
class UserControllerIT extends BaseIntegrationTest {

    private final MockMvc mockMvc;
    private final UserService userService;
    private final UserRepository userRepository;
    private final SecurityContextHelper securityContextHelper;
    private final EntityManager entityManager;

    private User savedClient;

    @BeforeEach
    void init() {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        var userPrincipal = securityContextHelper.authenticateUser(Role.CLIENT, securityContext);
        savedClient = userRepository.getReferenceById(userPrincipal.getId());
    }

    @Test
    void profile() throws Exception {
        UserReadDto expectedUserReadDto = UserReadDto.builder()
                .id(savedClient.getId())
                .username(savedClient.getUsername())
                .firstname(savedClient.getFirstname())
                .lastname(savedClient.getLastname())
                .role(savedClient.getRole())
                .gender(savedClient.getGender())
                .status(savedClient.getStatus())
                .birthDate(savedClient.getBirthDate())
                .build();

        mockMvc.perform(
                        get("/users/profile")
                )
                .andExpectAll(
                        status().is2xxSuccessful(),
                        model().attribute("user", expectedUserReadDto)
                );
    }

    @Test
    void registrationView() throws Exception {
        mockMvc.perform(get("/users/registration"))
                .andExpectAll(
                        status().is2xxSuccessful(),
                        view().name("user/registration")
                );
    }

    @Test
    void findById() throws Exception {
        UserReadDto expectedUserReadDto = UserReadDto.builder()
                .id(savedClient.getId())
                .username(savedClient.getUsername())
                .firstname(savedClient.getFirstname())
                .lastname(savedClient.getLastname())
                .role(savedClient.getRole())
                .gender(savedClient.getGender())
                .status(savedClient.getStatus())
                .birthDate(savedClient.getBirthDate())
                .build();

        mockMvc.perform(
                        get("/users/" + savedClient.getId())
                )
                .andExpectAll(
                        status().is2xxSuccessful(),
                        model().attribute("user", expectedUserReadDto)
                );
    }

    @Test
    void create() throws Exception {
        var userCreateDto = new UserCreateDto(
                "username_test",
                "test",
                "test",
                "test_test",
                Role.OWNER,
                Gender.MALE,
                LocalDate.of(2001, 1, 1)
        );

        mockMvc.perform(
                        post("/users/sign-up")
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
                        redirectedUrl("/login")
                );

        var createdUser = userRepository.findByUsername(userCreateDto.getUsername());

        createdUser.ifPresentOrElse((user -> {
                    assertThat(user.getUsername()).isEqualTo(userCreateDto.getUsername());
                    assertThat(user.getFirstname()).isEqualTo(userCreateDto.getFirstname());
                    assertThat(user.getLastname()).isEqualTo(userCreateDto.getLastname());
                    assertThat(user.getRole()).isEqualTo(userCreateDto.getRole());
                    assertThat(user.getGender()).isEqualTo(userCreateDto.getGender());
                    assertThat(user.getBirthDate()).isEqualTo(userCreateDto.getBirthDate());
                    assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
                }),
                Assertions::fail);
    }

    @Test
    void update() throws Exception {
        var userUpdateDto = UserUpdateDto.builder()
                .username("username_updated")
                .firstname("firstname_updated")
                .lastname("lastname_updated")
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(1999, 9, 9))
                .build();

        mockMvc.perform(
                        multipart("/users/update")
                                .file(getMultipartFile())
                                .param(username, userUpdateDto.getUsername())
                                .param(firstname, userUpdateDto.getFirstname())
                                .param(lastname, userUpdateDto.getLastname())
                                .param(gender, userUpdateDto.getGender().toString())
                                .param(birthDate, userUpdateDto.getBirthDate().toString())
                                .with(csrf())
                )
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrlPattern("/users/*")
                );

        var updatedUser = userService.findById(savedClient.getId());

        UserReadDto expectedUserReadDto = UserReadDto.builder()
                .id(savedClient.getId())
                .username(userUpdateDto.getUsername())
                .firstname(userUpdateDto.getFirstname())
                .lastname(userUpdateDto.getLastname())
                .role(savedClient.getRole())
                .gender(userUpdateDto.getGender())
                .birthDate(userUpdateDto.getBirthDate())
                .status(UserStatus.ACTIVE)
                .avatarMediaItemId(updatedUser.getAvatarMediaItemId())
                .build();

        assertThat(expectedUserReadDto).isEqualTo(updatedUser);
        assertThat(expectedUserReadDto.getAvatarMediaItemId()).isNotNull();
    }

    @Test
    void deleteAvatar() throws Exception {
        var updateDto = UserUpdateDto.builder()
                .username(savedClient.getUsername())
                .firstname(savedClient.getFirstname())
                .lastname(savedClient.getLastname())
                .gender(savedClient.getGender())
                .birthDate(savedClient.getBirthDate())
                .image(getMultipartFile())
                .build();
        userService.update(savedClient.getId(), updateDto);

        mockMvc.perform(
                        post("/users/" + savedClient.getId() + "/avatar/delete")
                                .with(csrf())
                )
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/users/profile")
                );

        entityManager.detach(savedClient);
        var updatedUser = userService.findById(savedClient.getId());

        assertThat(updatedUser.getAvatarMediaItemId()).isNull();
    }

    @Test
    void selfDelete() throws Exception {
        mockMvc.perform(
                        post("/users/" + savedClient.getId() + "/delete")
                                .with(csrf())
                )
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/login")
                );

        var deletedUser = userRepository.findById(savedClient.getId());

        assertTrue(deletedUser.isEmpty());
    }

    @Test
    void changePassword() throws Exception {
        var updatedPassword = "new_password";
        mockMvc.perform(
                        post("/users/change-password")
                                .param(oldPassword, "test")
                                .param(newPassword, updatedPassword)
                                .with(csrf())
                )
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/users/profile")
                );

        mockMvc.perform(
                        post("/login")
                                .param("username", savedClient.getUsername())
                                .param("password", updatedPassword)
                                .with(csrf())
                )
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/users/profile")
                )
                .andReturn();
    }

    @Test
    void changePasswordFail() throws Exception {
        var updatedPassword = "new_password";
        mockMvc.perform(
                        post("/users/change-password")
                                .param(oldPassword, "incorrect_password")
                                .param(newPassword, updatedPassword)
                                .with(csrf())
                )
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/users/profile"),
                        flash().attribute("passwordError", "incorrect current password")
                );

        mockMvc.perform(
                        post("/login")
                                .param("username", savedClient.getUsername())
                                .param("password", updatedPassword)
                                .with(csrf())
                )
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/login?error")
                )
                .andReturn();
    }

    private MockMultipartFile getMultipartFile() throws IOException {
        var fileBytes = Files.readAllBytes(Path.of("src", "test", "resources", "test-image.png"));
        return new MockMultipartFile(
                "image",
                "test-image.png",
                "image/png",
                fileBytes);
    }
}
