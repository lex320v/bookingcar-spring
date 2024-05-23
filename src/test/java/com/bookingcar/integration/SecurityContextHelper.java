package com.bookingcar.integration;


import com.bookingcar.dto.UserPrincipalDto;
import com.bookingcar.entity.User;
import com.bookingcar.entity.enums.Gender;
import com.bookingcar.entity.enums.Role;
import com.bookingcar.entity.enums.UserStatus;
import com.bookingcar.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SecurityContextHelper {

    private final UserRepository userRepository;

    public UserPrincipalDto authenticateUser(Role role, SecurityContext securityContext) {
        var user = User.builder()
                .username("test_" + role.name())
                .firstname("test")
                .lastname("test")
                .password("{noop}test")
                .role(role)
                .gender(Gender.MALE)
                .status(UserStatus.ACTIVE)
                .birthDate(LocalDate.of(2001, 1, 1))
                .build();
        var savedUser = userRepository.save(user);

        UserPrincipalDto currentUser = new UserPrincipalDto(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getPassword(),
                true,
                List.of(savedUser.getRole()));
        var authenticationToken = new TestingAuthenticationToken(
                currentUser,
                currentUser.getPassword(),
                currentUser.getAuthorities());

        securityContext.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(securityContext);

        return currentUser;
    }

    public UserPrincipalDto authenticateUser(User user, SecurityContext securityContext) {
        UserPrincipalDto currentUser = new UserPrincipalDto(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                true,
                List.of(user.getRole()));
        var authenticationToken = new TestingAuthenticationToken(
                currentUser,
                currentUser.getPassword(),
                currentUser.getAuthorities());

        securityContext.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(securityContext);

        return currentUser;
    }
}
