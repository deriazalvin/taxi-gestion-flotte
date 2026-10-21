package com.example.taxi.service;

import com.example.taxi.dto.request.AssignRideRequest;
import com.example.taxi.dto.request.RideRequest;
import com.example.taxi.dto.response.CandidateResponse;
import com.example.taxi.dto.response.RideResponse;
import com.example.taxi.entity.RideStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RideService {

    Page<RideResponse> search(String keyword, RideStatus status, Pageable pageable);

    RideResponse getById(Long id);

    RideResponse create(RideRequest req);

    List<CandidateResponse> candidates(Long rideId);

    RideResponse assign(Long rideId, AssignRideRequest req);

    RideResponse start(Long rideId);

    RideResponse complete(Long rideId);

    RideResponse cancel(Long rideId, String reason);

    List<RideResponse> listWaiting();
}