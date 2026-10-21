package com.example.taxi.dto.response;

import com.example.taxi.entity.Client;

import java.time.LocalDateTime;

public record ClientResponse(
        Long id,
        String name,
        String phone,
        String email,
        String address,
        LocalDateTime createdAt
) {
    public static ClientResponse from(Client c) {
        return new ClientResponse(c.getId(), c.getName(), c.getPhone(), c.getEmail(),
                c.getAddress(), c.getCreatedAt());
    }
}