package com.bookingcar.dto.user;

import com.bookingcar.entity.enums.Role;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserFilterDto {
    String firstname;
    String lastname;
    String username;
    Role role;
}
