package com.bookingcar.dto.user;

import com.bookingcar.entity.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Value
@FieldNameConstants
@Builder
public class UserUpdateDto {
    @NotBlank
    @Size(min = 3, max = 20)
    String username;

    @NotBlank
    @Size(min = 3, max = 20)
    String firstname;

    @NotBlank
    @Size(min = 3, max = 20)
    String lastname;

    @NotNull
    Gender gender;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate birthDate;

    MultipartFile image;
}
