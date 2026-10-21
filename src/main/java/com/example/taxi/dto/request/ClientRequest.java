package com.example.taxi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClientRequest(
        @NotBlank @Size(max = 80) String name,
        @NotBlank @Size(max = 20) String phone,
        @Email @Size(max = 120) String email,
        @Size(max = 160) String address
) {
}