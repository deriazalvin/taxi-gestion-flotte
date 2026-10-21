package com.example.taxi.service;

import com.example.taxi.dto.request.ClientRequest;
import com.example.taxi.dto.response.ClientResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
public interface ClientService {

    Page<ClientResponse> search(String keyword, Pageable pageable);

    List<ClientResponse> listAll();

    ClientResponse getById(Long id);

    ClientResponse create(ClientRequest req);

    ClientResponse update(Long id, ClientRequest req);

    void delete(Long id);
}