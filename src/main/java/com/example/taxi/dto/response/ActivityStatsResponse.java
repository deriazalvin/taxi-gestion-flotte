package com.example.taxi.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ActivityStatsResponse(
        LocalDate from,
        LocalDate to,
        long totalRides,
        long cancelledRides,
        BigDecimal cancellationRate,
        BigDecimal averageRevenue,
        BigDecimal totalDistanceKm,
        String mostActiveDriver,
        long mostActiveDriverRides,
        String mostUsedVehicle,
        long mostUsedVehicleRides,
        List<ActivityByDayResponse> byDay
) {
}