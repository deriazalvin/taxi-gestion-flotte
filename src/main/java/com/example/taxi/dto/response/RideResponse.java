package com.example.taxi.dto.response;

import com.example.taxi.entity.Ride;
import com.example.taxi.entity.RideStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RideResponse(
        Long id,
        String reference,
        String clientName,
        String clientPhone,
        Long clientId,
        String pickupLocation,
        String destination,
        BigDecimal distance,
        LocalDateTime rideDateTime,
        BigDecimal fare,
        RideStatus status,
        Long driverId,
        String driverName,
        Long vehicleId,
        String vehiclePlate,
        Integer assignmentScore,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        String cancelReason
) {
    public static RideResponse from(Ride r) {
        return new RideResponse(r.getId(), r.getReference(), r.getClientName(), r.getClientPhone(),
                r.getClient() != null ? r.getClient().getId() : null,
                r.getPickupLocation(), r.getDestination(), r.getDistance(), r.getRideDateTime(), r.getFare(),
                r.getStatus(),
                r.getDriver() != null ? r.getDriver().getId() : null,
                r.getDriver() != null ? r.getDriver().fullName() : null,
                r.getVehicle() != null ? r.getVehicle().getId() : null,
                r.getVehicle() != null ? r.getVehicle().getPlateNumber() : null,
                r.getAssignmentScore(), r.getStartedAt(), r.getCompletedAt(), r.getCancelReason());
    }
}