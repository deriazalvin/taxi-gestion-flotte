package com.example.taxi.dto.request;

import com.example.taxi.entity.VehicleStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record VehicleRequest(
        @NotBlank @Size(max = 20) String plateNumber,
        @NotBlank @Size(max = 60) String brand,
        @NotBlank @Size(max = 60) String model,
        @NotNull @Min(1950) @Max(2100) Integer modelYear,
        @NotNull @Min(1) @Max(20) Integer capacity,
        @NotNull @DecimalMin("0") BigDecimal mileage,
        @NotNull VehicleStatus status
) {
}