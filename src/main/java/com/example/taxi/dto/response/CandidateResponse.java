package com.example.taxi.dto.response;

import java.math.BigDecimal;

public record CandidateResponse(
        Long driverId,
        String driverName,
        String vehiclePlate,
        Integer capacity,
        BigDecimal score,
        BigDecimal loadPoints,
        BigDecimal seniorityPoints,
        BigDecimal vehiclePoints
) {
}