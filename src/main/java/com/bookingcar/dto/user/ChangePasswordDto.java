package com.bookingcar.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

@Value
@FieldNameConstants
public class ChangePasswordDto {
    @NotBlank
    @Size(min = 3, max = 20)
    String oldPassword;

    @NotBlank
    @Size(min = 3, max = 20)
    String newPassword;
}
