package com.example.taxi.service;

import com.example.taxi.dto.request.ClientRequest;
import com.example.taxi.dto.request.RideRequest;
import com.example.taxi.dto.response.ClientResponse;
import com.example.taxi.entity.Client;
import com.example.taxi.exception.ConflictException;
import com.example.taxi.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import com.example.taxi.TaxiApplication;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest

@Transactional
class ClientServiceIntegrationTest {

    @Autowired
    private ClientService clientService;
    @Autowired
    private RideService rideService;
    @Autowired
    private com.example.taxi.repository.ClientRepository clientRepository;

    @Test
    void duplicatePhone_isRejected() {
        String phone = "0600" + (1000 + (int) (System.nanoTime() % 8999));
        clientService.create(new ClientRequest("Client A", phone, "a@test.fr", "Paris"));

        assertThatThrownBy(() -> clientService.create(
                new ClientRequest("Client B", phone, "b@test.fr", "Lyon")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void delete_isBlockedWhenRideHistoryExists() {
        String phone = "0600" + (1000 + (int) (System.nanoTime() % 8999));
        ClientResponse client = clientService.create(
                new ClientRequest("Client A", phone, "a@test.fr", "Paris"));

        rideService.create(new RideRequest("Client A", phone, "Gare", "Aéroport",
                new BigDecimal("8.0"), LocalDateTime.now().plusHours(1),
                new BigDecimal("25.00"), client.id()));

        assertThatThrownBy(() -> clientService.delete(client.id()))
                .isInstanceOf(ConflictException.class);

        Client loaded = clientRepository.findById(client.id()).orElseThrow();
        assertThat(loaded.getName()).isEqualTo("Client A");
    }

    @Test
    void delete_succeedsWithoutRides() {
        String phone = "0600" + (1000 + (int) (System.nanoTime() % 8999));
        ClientResponse client = clientService.create(
                new ClientRequest("Client Itinérant", phone, "c@test.fr", "Nice"));

        clientService.delete(client.id());
        clientRepository.flush();

        assertThat(clientRepository.findById(client.id())).isEmpty();
        assertThatThrownBy(() -> clientService.getById(client.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}