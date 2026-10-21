package com.example.taxi.controller.web;

import com.example.taxi.dto.request.DriverRequest;
import com.example.taxi.entity.DriverStatus;
import com.example.taxi.service.DriverService;
import com.example.taxi.service.VehicleService;
import com.example.taxi.util.WebSortSupport;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/drivers")
public class DriverController {

    private final DriverService driverService;
    private final VehicleService vehicleService;

    public DriverController(DriverService driverService, VehicleService vehicleService) {
        this.driverService = driverService;
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) DriverStatus status,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false, defaultValue = "lastName,asc") String sort,
                       Model model) {
        model.addAttribute("page", driverService.search(keyword, status,
                PageRequest.of(page, 10, WebSortSupport.toSort(sort, "lastName", true,
                        "firstName", "lastName", "phone", "licenseNumber",
                        "licenseExpiryDate", "status", "hireDate", "commissionRate"))));
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("statuses", DriverStatus.values());
        model.addAttribute("sort", sort);
        return "drivers/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public String form(Model model) {
        model.addAttribute("driver", new DriverForm());
        model.addAttribute("statuses", DriverStatus.values());
        model.addAttribute("vehicles", vehicleService.listAll());
        return "drivers/form";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public String edit(@PathVariable Long id, Model model) {
        var d = driverService.getById(id);
        model.addAttribute("driver", new DriverForm(d));
        model.addAttribute("statuses", DriverStatus.values());
        model.addAttribute("vehicles", vehicleService.listAll());
        return "drivers/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public String save(@RequestParam(required = false) Long id,
                       @ModelAttribute("driver") DriverForm form,
                       RedirectAttributes ra) {
        DriverRequest req = form.toRequest();
        if (id == null) {
            var created = driverService.create(req);
            ra.addFlashAttribute("success", "Chauffeur « " + created.lastName() + " » créé.");
        } else {
            var updated = driverService.update(id, req);
            ra.addFlashAttribute("success", "Chauffeur « " + updated.lastName() + " » mis à jour.");
        }
        return "redirect:/drivers";
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public String status(@PathVariable Long id, @RequestParam DriverStatus status, RedirectAttributes ra) {
        driverService.setStatus(id, status);
        ra.addFlashAttribute("success", "Statut du chauffeur mis à jour : " + status);
        return "redirect:" + (status == DriverStatus.DISPONIBLE ? "/fleet" : "/drivers");
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        driverService.delete(id);
        ra.addFlashAttribute("success", "Chauffeur supprimé.");
        return "redirect:/drivers";
    }

    public record DriverForm(Long id, String firstName, String lastName, String phone,
                             String licenseNumber, String licenseExpiryDate, String hireDate,
                             DriverStatus status, Long vehicleId, java.math.BigDecimal commissionRate) {

        public DriverForm() {
            this(null, "", "", "", "", null, null, DriverStatus.DISPONIBLE, null, new java.math.BigDecimal("10.00"));
        }

        public DriverForm(com.example.taxi.dto.response.DriverResponse d) {
            this(d.id(), d.firstName(), d.lastName(), d.phone(), d.licenseNumber(),
                    d.licenseExpiryDate() != null ? d.licenseExpiryDate().toString() : null,
                    d.hireDate() != null ? d.hireDate().toString() : null,
                    d.status(), d.vehicleId(), d.commissionRate());
        }

        public DriverRequest toRequest() {
            return new DriverRequest(firstName, lastName, phone, licenseNumber,
                    licenseExpiryDate != null && !licenseExpiryDate.isBlank()
                            ? java.time.LocalDate.parse(licenseExpiryDate) : java.time.LocalDate.now().plusYears(1),
                    hireDate != null && !hireDate.isBlank()
                            ? java.time.LocalDate.parse(hireDate) : java.time.LocalDate.now(),
                    status, vehicleId, commissionRate);
        }
    }
}