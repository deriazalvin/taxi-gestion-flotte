package com.example.taxi.mapper;

import com.example.taxi.dto.request.RideRequest;
import com.example.taxi.entity.Ride;

public final class RideMapper {

    private RideMapper() {
    }

    public static Ride toEntity(RideRequest req, String reference) {
        return new Ride(reference, req.clientName(), req.clientPhone(), req.pickupLocation(),
                req.destination(), req.distance(), req.rideDateTime(), req.fare());
    }
}