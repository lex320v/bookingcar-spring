package com.bookingcar.entity.enums;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    SUPER_ADMIN,
    ADMIN,
    OWNER,
    CLIENT;

    @Override
    public String getAuthority() {
        return name();
    }
}
