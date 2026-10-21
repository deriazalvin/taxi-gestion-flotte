package com.example.taxi.controller.web;

import com.example.taxi.service.RideService;
import com.example.taxi.service.StatsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/stats")
public class StatsController {

    private final StatsService statsService;
    private final RideService rideService;

    public StatsController(StatsService statsService, RideService rideService) {
        this.statsService = statsService;
        this.rideService = rideService;
    }

    @GetMapping
    public String stats(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                        Model model) {
        LocalDate start = from != null ? from : LocalDate.now().minusDays(14);
        LocalDate end = to != null ? to : LocalDate.now();
        if (end.isBefore(start)) {
            end = start;
        }
        model.addAttribute("from", start);
        model.addAttribute("to", end);
        model.addAttribute("revenue", statsService.revenue(start, end));
        model.addAttribute("activity", statsService.activity(start, end));
        model.addAttribute("waitingRides", rideService.listWaiting());
        return "stats";
    }
}