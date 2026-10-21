package com.example.taxi.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RevenueStatsResponse(
        LocalDate from,
        LocalDate to,
        long totalRides,
        BigDecimal totalRevenue,
        BigDecimal totalCommission,
        BigDecimal totalDriverRevenue,
        BigDecimal averageRevenue,
        List<RevenueBreakdownResponse> byDriver,
        List<RevenueBreakdownResponse> byVehicle,
        List<RevenueByDayResponse> byDay
) {
}