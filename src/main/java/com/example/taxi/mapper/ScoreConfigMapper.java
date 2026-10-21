package com.example.taxi.mapper;

import com.example.taxi.dto.request.ScoreConfigRequest;
import com.example.taxi.entity.ScoreConfig;

public final class ScoreConfigMapper {

    private ScoreConfigMapper() {
    }

    public static void update(ScoreConfig config, ScoreConfigRequest req) {
        config.setLoadWeight(req.loadWeight());
        config.setSeniorityWeight(req.seniorityWeight());
        config.setVehicleWeight(req.vehicleWeight());
    }
}