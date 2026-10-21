package com.example.taxi.service.impl;

import com.example.taxi.dto.request.ScoreConfigRequest;
import com.example.taxi.dto.response.ScoreConfigResponse;
import com.example.taxi.entity.ScoreConfig;
import com.example.taxi.mapper.ScoreConfigMapper;
import com.example.taxi.repository.ScoreConfigRepository;
import com.example.taxi.service.ScoreConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScoreConfigServiceImpl implements ScoreConfigService {

    private final ScoreConfigRepository configRepository;

    public ScoreConfigServiceImpl(ScoreConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Override
    @Transactional
    public ScoreConfigResponse get() {
        return ScoreConfigResponse.from(current());
    }

    @Override
    @Transactional
    public ScoreConfigResponse update(ScoreConfigRequest req) {
        ScoreConfig config = current();
        ScoreConfigMapper.update(config, req);
        return ScoreConfigResponse.from(configRepository.save(config));
    }

    public ScoreConfig current() {
        return configRepository.findAll().stream().findFirst()
                .orElseGet(() -> configRepository.save(new ScoreConfig(null, null, null)));
    }
}