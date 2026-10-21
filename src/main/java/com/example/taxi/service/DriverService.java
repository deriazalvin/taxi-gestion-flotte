package com.example.taxi.service;

import com.example.taxi.dto.request.DriverRequest;
import com.example.taxi.dto.response.DriverResponse;
import com.example.taxi.entity.DriverStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DriverService {

    Page<DriverResponse> search(String keyword, DriverStatus status, Pageable pageable);

    List<DriverResponse> listAll();

    DriverResponse getById(Long id);

    DriverResponse create(DriverRequest req);

    DriverResponse update(Long id, DriverRequest req);

    DriverResponse setStatus(Long id, DriverStatus status);

    void delete(Long id);
}