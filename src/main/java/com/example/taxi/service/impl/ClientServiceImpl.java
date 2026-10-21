package com.example.taxi.service.impl;

import com.example.taxi.dto.request.ClientRequest;
import com.example.taxi.dto.response.ClientResponse;
import com.example.taxi.entity.Client;
import com.example.taxi.exception.ConflictException;
import com.example.taxi.exception.ResourceNotFoundException;
import com.example.taxi.mapper.ClientMapper;
import com.example.taxi.repository.ClientRepository;
import com.example.taxi.repository.RideRepository;
import com.example.taxi.service.ClientService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final RideRepository rideRepository;

    public ClientServiceImpl(ClientRepository clientRepository, RideRepository rideRepository) {
        this.clientRepository = clientRepository;
        this.rideRepository = rideRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientResponse> search(String keyword, Pageable pageable) {
        return clientRepository.findAll((root, q, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (keyword != null && !keyword.isBlank()) {
                String like = keyword.toLowerCase();
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), "%" + like + "%"),
                        cb.like(cb.lower(root.get("phone")), "%" + like + "%"),
                        cb.like(cb.lower(root.get("email")), "%" + like + "%")));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        }, pageable).map(ClientResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientResponse> listAll() {
        return clientRepository.findAll().stream().map(ClientResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse getById(Long id) {
        return ClientResponse.from(findEntity(id));
    }

    @Override
    @Transactional
    public ClientResponse create(ClientRequest req) {
        if (clientRepository.existsByPhone(req.phone())) {
            throw new ConflictException("Un client avec le téléphone « " + req.phone() + " » existe déjà.");
        }
        return ClientResponse.from(clientRepository.save(ClientMapper.toEntity(req)));
    }

    @Override
    @Transactional
    public ClientResponse update(Long id, ClientRequest req) {
        Client client = findEntity(id);
        clientRepository.findByPhone(req.phone())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new ConflictException("Un client avec le téléphone « " + req.phone() + " » existe déjà.");
                });
        ClientMapper.update(client, req);
        return ClientResponse.from(clientRepository.save(client));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Client client = findEntity(id);
        if (rideRepository.countByClientId(id) > 0) {
            throw new ConflictException("Impossible de supprimer ce client : un historique de courses existe.");
        }
        clientRepository.delete(client);
    }

    @Transactional(readOnly = true)
    public Client findEntity(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable : id " + id));
    }
}