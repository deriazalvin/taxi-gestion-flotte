package com.example.taxi.dto.response;

import com.example.taxi.entity.Driver;
import com.example.taxi.entity.DriverStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DriverResponse(
        Long id,
        String firstName,
        String lastName,
        String phone,
        String licenseNumber,
        LocalDate licenseExpiryDate,
        boolean licenseValid,
        DriverStatus status,
        LocalDate hireDate,
        BigDecimal commissionRate,
        Long vehicleId,
        String vehiclePlate,
        Long activeRideId,
        int ridesToday
) {
    public static DriverResponse from(Driver d) {
        return new DriverResponse(d.getId(), d.getFirstName(), d.getLastName(), d.getPhone(),
                d.getLicenseNumber(), d.getLicenseExpiryDate(), d.hasValidLicense(), d.getStatus(),
                d.getHireDate(), d.getCommissionRate(),
                d.getVehicle() != null ? d.getVehicle().getId() : null,
                d.getVehicle() != null ? d.getVehicle().getPlateNumber() : null,
                null, 0);
    }
}