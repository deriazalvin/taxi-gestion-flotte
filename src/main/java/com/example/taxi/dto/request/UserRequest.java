package com.example.taxi.dto.request;

import com.example.taxi.security.AppRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank @Size(max = 60) String username,
        @NotBlank @Size(min = 4, max = 60) String password,
        @NotBlank @Size(max = 120) String displayName,
        @NotNull AppRole role
) {
}