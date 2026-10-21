package com.example.taxi.service;

import com.example.taxi.dto.request.VehicleRequest;
import com.example.taxi.dto.response.VehicleResponse;
import com.example.taxi.entity.VehicleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VehicleService {

    Page<VehicleResponse> search(String keyword, VehicleStatus status, Pageable pageable);

    List<VehicleResponse> listAll();

    List<VehicleResponse> listAvailable();

    VehicleResponse getById(Long id);

    VehicleResponse create(VehicleRequest req);

    VehicleResponse update(Long id, VehicleRequest req);

    VehicleResponse setStatus(Long id, VehicleStatus status);

    void delete(Long id);
}