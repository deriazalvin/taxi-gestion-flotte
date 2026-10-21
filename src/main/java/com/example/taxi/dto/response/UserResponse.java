package com.example.taxi.dto.response;

import com.example.taxi.entity.AppUser;
import com.example.taxi.security.AppRole;

public record UserResponse(
        Long id,
        String username,
        String displayName,
        AppRole role,
        boolean enabled
) {
    public static UserResponse from(AppUser u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getDisplayName(), u.getRole(), u.isEnabled());
    }
}