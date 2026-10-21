package com.example.taxi.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RevenueByDayResponse(
        LocalDate date,
        BigDecimal revenue,
        BigDecimal driverRevenue,
        long rideCount
) {
}