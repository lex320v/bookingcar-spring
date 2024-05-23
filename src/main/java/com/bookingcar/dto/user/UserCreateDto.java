package com.bookingcar.dto.user;

import com.bookingcar.entity.enums.Gender;
import com.bookingcar.entity.enums.Role;
import com.bookingcar.validation.RoleValidator;
import com.bookingcar.validation.group.CreateAdmin;
import com.bookingcar.validation.group.CreateUser;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static com.bookingcar.entity.enums.Role.ADMIN;
import static com.bookingcar.entity.enums.Role.CLIENT;
import static com.bookingcar.entity.enums.Role.OWNER;

@Value
@FieldNameConstants
public class UserCreateDto {

    @NotBlank
    @Size(min = 3, max = 20)
    String username;

    @NotBlank
    @Size(min = 3, max = 20)
    String password;

    @NotBlank
    @Size(min = 3, max = 20)
    String firstname;

    @NotBlank
    @Size(min = 3, max = 20)
    String lastname;

    @RoleValidator(allowedValues = {ADMIN}, groups = {CreateAdmin.class})
    @RoleValidator(allowedValues = {OWNER, CLIENT}, groups = {CreateUser.class})
    Role role;

    @NotNull
    Gender gender;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate birthDate;
}
