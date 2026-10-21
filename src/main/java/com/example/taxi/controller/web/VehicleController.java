package com.example.taxi.controller.web;

import com.example.taxi.dto.request.VehicleRequest;
import com.example.taxi.dto.response.VehicleResponse;
import com.example.taxi.entity.VehicleStatus;
import com.example.taxi.service.VehicleService;
import com.example.taxi.util.WebSortSupport;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) VehicleStatus status,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false, defaultValue = "plateNumber,asc") String sort,
                       Model model) {
        model.addAttribute("page", vehicleService.search(keyword, status,
                PageRequest.of(page, 10, WebSortSupport.toSort(sort, "plateNumber", true,
                        "plateNumber", "brand", "model", "modelYear",
                        "capacity", "mileage", "status"))));
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("statuses", VehicleStatus.values());
        model.addAttribute("sort", sort);
        return "vehicles/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public String form(Model model) {
        model.addAttribute("vehicle", new VehicleForm());
        model.addAttribute("statuses", VehicleStatus.values());
        return "vehicles/form";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public String edit(@PathVariable Long id, Model model) {
        var v = vehicleService.getById(id);
        model.addAttribute("vehicle", new VehicleForm(v));
        model.addAttribute("statuses", VehicleStatus.values());
        return "vehicles/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public String save(@RequestParam(required = false) Long id,
                       @ModelAttribute("vehicle") VehicleForm form,
                       RedirectAttributes ra) {
        VehicleRequest req = new VehicleRequest(form.plateNumber(), form.brand(), form.model(),
                form.modelYear(), form.capacity(), form.mileage(), form.status());
        if (id == null) {
            var created = vehicleService.create(req);
            ra.addFlashAttribute("success", "Véhicule « " + created.plateNumber() + " » créé.");
        } else {
            vehicleService.update(id, req);
            ra.addFlashAttribute("success", "Véhicule mis à jour.");
        }
        return "redirect:/vehicles";
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public String status(@PathVariable Long id, @RequestParam VehicleStatus status, RedirectAttributes ra) {
        vehicleService.setStatus(id, status);
        ra.addFlashAttribute("success", "Statut du véhicule mis à jour : " + status);
        return "redirect:/fleet";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        vehicleService.delete(id);
        ra.addFlashAttribute("success", "Véhicule supprimé.");
        return "redirect:/vehicles";
    }

    public record VehicleForm(Long id, String plateNumber, String brand, String model,
                              Integer modelYear, Integer capacity, java.math.BigDecimal mileage,
                              VehicleStatus status) {

        public VehicleForm() {
            this(null, "", "", "", java.time.LocalDate.now().getYear() - 3, 4,
                    new java.math.BigDecimal("0"), VehicleStatus.DISPONIBLE);
        }

        public VehicleForm(VehicleResponse v) {
            this(v.id(), v.plateNumber(), v.brand(), v.model(), v.modelYear(), v.capacity(), v.mileage(), v.status());
        }
    }
}