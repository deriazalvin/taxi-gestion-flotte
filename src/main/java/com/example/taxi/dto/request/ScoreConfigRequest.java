package com.example.taxi.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ScoreConfigRequest(
        @NotNull @DecimalMin("0.1") @DecimalMax("10.0") BigDecimal loadWeight,
        @NotNull @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal seniorityWeight,
        @NotNull @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal vehicleWeight
) {
}