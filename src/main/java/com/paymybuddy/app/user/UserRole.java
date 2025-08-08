package com.paymybuddy.app.user;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public enum UserRole implements GrantedAuthority {
    ADMIN("ROLE_ADMIN", "Administrator"),
    MANAGER("ROLE_MANAGER", "Manager"),
    USER("ROLE_USER", "Regular User");

    private final String authority;
    private final String displayName;

    UserRole(String authority, String displayName) {
        this.authority = authority;
        this.displayName = displayName;
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static String getHighest(Collection<? extends GrantedAuthority> roles) {
        if (roles.stream().anyMatch(a -> a.equals(ADMIN))) {
            return ADMIN.getAuthority();
        } else if (roles.stream().anyMatch(a -> a.equals(MANAGER))) {
            return MANAGER.getAuthority();
        } else {
            return USER.getAuthority();
        }
    }

    public static Set<UserRole> expandHighest(String highestRole) {
        Set<UserRole> roles = new HashSet<>();

        switch (highestRole) {
            case "ROLE_ADMIN":
                roles.add(ADMIN);
            case "ROLE_MANAGER":
                roles.add(MANAGER);
            default:
                roles.add(USER);
        }

        return roles;
    }
}