package com.example.taxi.controller.web;

import com.example.taxi.entity.DriverStatus;
import com.example.taxi.entity.RideStatus;
import com.example.taxi.repository.RideRepository;
import com.example.taxi.service.DriverService;
import com.example.taxi.service.RideService;
import com.example.taxi.service.StatsService;
import com.example.taxi.service.VehicleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
public class DashboardController {

    private final RideService rideService;
    private final RideRepository rideRepository;
    private final DriverService driverService;
    private final VehicleService vehicleService;
    private final StatsService statsService;

    public DashboardController(RideService rideService, RideRepository rideRepository,
                               DriverService driverService, VehicleService vehicleService,
                               StatsService statsService) {
        this.rideService = rideService;
        this.rideRepository = rideRepository;
        this.driverService = driverService;
        this.vehicleService = vehicleService;
        this.statsService = statsService;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LocalDateTime nextDay = LocalDate.now().plusDays(1).atStartOfDay();

        model.addAttribute("waitingCount", rideRepository.countByStatus(RideStatus.EN_ATTENTE));
        model.addAttribute("activeCount", rideRepository.countByStatus(RideStatus.ASSIGNEE)
                + rideRepository.countByStatus(RideStatus.EN_COURS));
        model.addAttribute("ridesToday", rideRepository.countCompletedInRange(dayStart, nextDay));
        model.addAttribute("cancelledToday", rideRepository.countCanceledBetween(dayStart, nextDay));
        model.addAttribute("driversAvailable", driverService.listAll().stream()
                .filter(d -> d.status() == DriverStatus.DISPONIBLE && d.licenseValid()).count());
        model.addAttribute("vehiclesAvailable", vehicleService.listAvailable().size());
        model.addAttribute("waitingRides", rideService.listWaiting());
        model.addAttribute("stats", statsService.activity(LocalDate.now().minusDays(13), LocalDate.now()));
        return "dashboard";
    }
}