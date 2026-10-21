package com.example.taxi.controller.rest;

import com.example.taxi.dto.response.ActivityStatsResponse;
import com.example.taxi.dto.response.RevenueStatsResponse;
import com.example.taxi.service.StatsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/statistics")
public class StatsRestController {

    private final StatsService statsService;

    public StatsRestController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/revenue")
    public RevenueStatsResponse revenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return statsService.revenue(from, to);
    }

    @GetMapping("/activity")
    public ActivityStatsResponse activity(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return statsService.activity(from, to);
    }
}