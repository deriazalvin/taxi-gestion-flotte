package com.example.taxi.service;

import com.example.taxi.dto.response.ActivityStatsResponse;
import com.example.taxi.dto.response.RevenueStatsResponse;

import java.time.LocalDate;

public interface StatsService {

    RevenueStatsResponse revenue(LocalDate from, LocalDate to);

    ActivityStatsResponse activity(LocalDate from, LocalDate to);
}