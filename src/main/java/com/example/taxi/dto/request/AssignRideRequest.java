package com.example.taxi.dto.request;


public record AssignRideRequest(
        Long driverId,
        Long vehicleId
) {
}