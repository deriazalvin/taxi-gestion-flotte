package com.example.taxi.mapper;

import com.example.taxi.dto.request.VehicleRequest;
import com.example.taxi.entity.Vehicle;

public final class VehicleMapper {

    private VehicleMapper() {
    }

    public static Vehicle toEntity(VehicleRequest req) {
        Vehicle vehicle = new Vehicle(req.plateNumber(), req.brand(), req.model(),
                req.modelYear(), req.capacity(), req.mileage());
        vehicle.setStatus(req.status());
        return vehicle;
    }

    public static void update(Vehicle vehicle, VehicleRequest req) {
        vehicle.setPlateNumber(req.plateNumber());
        vehicle.setBrand(req.brand());
        vehicle.setModel(req.model());
        vehicle.setModelYear(req.modelYear());
        vehicle.setCapacity(req.capacity());
        vehicle.setMileage(req.mileage());
        vehicle.setStatus(req.status());
    }
}