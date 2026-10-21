package com.example.taxi.dto.response;

import com.example.taxi.entity.Vehicle;
import com.example.taxi.entity.VehicleStatus;

import java.math.BigDecimal;

public record VehicleResponse(
        Long id,
        String plateNumber,
        String brand,
        String model,
        Integer modelYear,
        Integer capacity,
        BigDecimal mileage,
        VehicleStatus status,
        Long activeRideId
) {
    public static VehicleResponse from(Vehicle v) {
        return new VehicleResponse(v.getId(), v.getPlateNumber(), v.getBrand(), v.getModel(),
                v.getModelYear(), v.getCapacity(), v.getMileage(), v.getStatus(), null);
    }
}