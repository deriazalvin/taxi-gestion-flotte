package com.example.taxi.service;

import com.example.taxi.dto.request.ScoreConfigRequest;
import com.example.taxi.dto.response.ScoreConfigResponse;

public interface ScoreConfigService {

    ScoreConfigResponse get();

    ScoreConfigResponse update(ScoreConfigRequest req);
}