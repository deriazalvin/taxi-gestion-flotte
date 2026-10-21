package com.example.taxi.dto.response;

import java.math.BigDecimal;

public record RevenueBreakdownResponse(
        String label,
        Long id,
        BigDecimal revenue,
        long rideCount
) {
}