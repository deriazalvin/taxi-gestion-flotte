package com.example.taxi.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ActivityByDayResponse(
        LocalDate date,
        long rideCount,
        long cancelledCount,
        BigDecimal revenue,
        BigDecimal distanceKm
) {
}