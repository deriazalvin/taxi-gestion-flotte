package com.example.taxi.mapper;

import com.example.taxi.dto.request.ClientRequest;
import com.example.taxi.entity.Client;

public final class ClientMapper {

    private ClientMapper() {
    }

    public static Client toEntity(ClientRequest req) {
        return new Client(req.name(), req.phone(), req.email(), req.address());
    }

    public static void update(Client client, ClientRequest req) {
        client.setName(req.name());
        client.setPhone(req.phone());
        client.setEmail(req.email());
        client.setAddress(req.address());
    }
}