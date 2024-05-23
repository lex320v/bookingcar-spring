package com.bookingcar.dto;

import lombok.ToString;
import lombok.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Value
@ToString(callSuper = true)
public class UserPrincipalDto extends User {
    Long id;

    public UserPrincipalDto(Long id,
                            String username,
                            String password,
                            boolean enabled,
                            Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, true, true, true, authorities);
        this.id = id;
    }
}
