package com.example.taxi.controller.web;

import com.example.taxi.entity.DriverStatus;
import com.example.taxi.entity.RideStatus;
import com.example.taxi.repository.RideRepository;
import com.example.taxi.service.DriverService;
import com.example.taxi.service.VehicleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/fleet")
public class FleetController {

    private final DriverService driverService;
    private final VehicleService vehicleService;
    private final RideRepository rideRepository;

    public FleetController(DriverService driverService, VehicleService vehicleService,
                           RideRepository rideRepository) {
        this.driverService = driverService;
        this.vehicleService = vehicleService;
        this.rideRepository = rideRepository;
    }

    @GetMapping
    public String fleet(Model model) {
        model.addAttribute("drivers", driverService.listAll());
        model.addAttribute("vehicles", vehicleService.listAll());
        model.addAttribute("driversAvailable", driverService.listAll().stream()
                .filter(d -> d.status() == DriverStatus.DISPONIBLE && d.licenseValid()).count());
        model.addAttribute("vehiclesAvailable", vehicleService.listAvailable().size());
        model.addAttribute("expiredLicenses", driverService.listAll().stream()
                .filter(d -> !d.licenseValid()).count());
        model.addAttribute("activeRides", rideRepository.countByStatus(RideStatus.ASSIGNEE)
                + rideRepository.countByStatus(RideStatus.EN_COURS));
        return "fleet";
    }
}