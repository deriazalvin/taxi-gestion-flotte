package com.example.taxi.dto.response;

import com.example.taxi.entity.ScoreConfig;

import java.math.BigDecimal;

public record ScoreConfigResponse(
        Long id,
        BigDecimal loadWeight,
        BigDecimal seniorityWeight,
        BigDecimal vehicleWeight
) {
    public static ScoreConfigResponse from(ScoreConfig c) {
        return new ScoreConfigResponse(c.getId(), c.getLoadWeight(), c.getSeniorityWeight(), c.getVehicleWeight());
    }
}