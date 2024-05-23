package com.bookingcar.dto.user;

import com.bookingcar.entity.enums.Gender;
import com.bookingcar.entity.enums.Role;
import com.bookingcar.entity.enums.UserStatus;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.time.LocalDate;

@Value
@RequiredArgsConstructor
@Builder
public class UserReadDto {
    Long id;
    String username;
    String firstname;
    String lastname;
    LocalDate birthDate;
    Role role;
    Gender gender;
    UserStatus status;
    Long avatarMediaItemId;
}
