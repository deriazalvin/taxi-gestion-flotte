package com.example.taxi.dto.request;

import com.example.taxi.entity.DriverStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DriverRequest(
        @NotBlank @Size(max = 60) String firstName,
        @NotBlank @Size(max = 60) String lastName,
        @NotBlank @Size(max = 20) String phone,
        @NotBlank @Size(max = 30) String licenseNumber,
        @NotNull LocalDate licenseExpiryDate,
        @NotNull LocalDate hireDate,
        @NotNull DriverStatus status,
        Long vehicleId,
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal commissionRate
) {
}