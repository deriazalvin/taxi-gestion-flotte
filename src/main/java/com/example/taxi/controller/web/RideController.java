package com.example.taxi.controller.web;

import com.example.taxi.dto.request.RideRequest;
import com.example.taxi.entity.RideStatus;
import com.example.taxi.service.RideService;
import com.example.taxi.util.WebSortSupport;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/rides")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) RideStatus status,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false, defaultValue = "rideDateTime,desc") String sort,
                       Model model) {
        model.addAttribute("page", rideService.search(keyword, status,
                PageRequest.of(page, 10, WebSortSupport.toSort(sort, "rideDateTime", false,
                        "reference", "clientName", "pickupLocation", "destination",
                        "distance", "rideDateTime", "fare", "status"))));
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("statuses", RideStatus.values());
        model.addAttribute("sort", sort);
        return "rides/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public String form(Model model) {
        model.addAttribute("ride", new RideForm());
        return "rides/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public String save(@ModelAttribute("ride") RideForm form, RedirectAttributes ra) {
        LocalDateTime when = form.rideDateTime() != null ? form.rideDateTime()
                : LocalDateTime.now().plusHours(1);
        RideRequest req = new RideRequest(form.clientName(), form.clientPhone(),
                form.pickupLocation(), form.destination(), form.distance(), when, form.fare());
        var created = rideService.create(req);
        ra.addFlashAttribute("success", "Course « " + created.reference() + " » enregistrée.");
        return "redirect:/rides/" + created.id();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("ride", rideService.getById(id));
        return "rides/detail";
    }

    @GetMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public String assignPage(@PathVariable Long id, Model model) {
        model.addAttribute("ride", rideService.getById(id));
        model.addAttribute("candidates", rideService.candidates(id));
        return "rides/assign";
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public String assign(@PathVariable Long id,
                         @RequestParam(required = false) Long driverId,
                         RedirectAttributes ra) {
        var ride = rideService.assign(id,
                driverId != null ? new com.example.taxi.dto.request.AssignRideRequest(driverId, null) : null);
        String who = "automatique";
        if (driverId != null) {
            who = ride.driverName() != null ? ride.driverName() : "manuelle";
        }
        ra.addFlashAttribute("success", "Course « " + ride.reference() + " » assignée (" + who + ").");
        return "redirect:/rides/" + id;
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public String start(@PathVariable Long id, RedirectAttributes ra) {
        var ride = rideService.start(id);
        ra.addFlashAttribute("success", "Course « " + ride.reference() + " » démarrée.");
        return "redirect:/rides/" + id;
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public String complete(@PathVariable Long id, RedirectAttributes ra) {
        var ride = rideService.complete(id);
        ra.addFlashAttribute("success", "Course « " + ride.reference() + " » terminée.");
        return "redirect:/rides/" + id;
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public String cancel(@PathVariable Long id, @RequestParam(required = false) String reason,
                         RedirectAttributes ra) {
        var ride = rideService.cancel(id, reason);
        ra.addFlashAttribute("success", "Course « " + ride.reference() + " » annulée.");
        return "redirect:/rides/" + id;
    }

    public record RideForm(Long id, String clientName, String clientPhone,
                           String pickupLocation, String destination,
                           BigDecimal distance, LocalDateTime rideDateTime, BigDecimal fare) {

        public RideForm() {
            this(null, "", "", "", "", new BigDecimal("10.0"), null, new BigDecimal("25.00"));
        }
    }
}