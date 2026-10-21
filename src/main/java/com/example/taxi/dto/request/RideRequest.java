package com.example.taxi.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RideRequest(
        @NotBlank @Size(max = 80) String clientName,
        @NotBlank @Size(max = 20) String clientPhone,
        @NotBlank @Size(max = 120) String pickupLocation,
        @NotBlank @Size(max = 120) String destination,
        @NotNull @DecimalMin("0.1") BigDecimal distance,
        @NotNull LocalDateTime rideDateTime,
        @NotNull @DecimalMin("0.01") BigDecimal fare,
        Long clientId
) {
    public RideRequest(String clientName, String clientPhone, String pickupLocation,
                       String destination, BigDecimal distance, LocalDateTime rideDateTime,
                       BigDecimal fare) {
        this(clientName, clientPhone, pickupLocation, destination, distance, rideDateTime, fare, null);
    }
}