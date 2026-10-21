package com.example.taxi.controller.web;

import com.example.taxi.dto.request.ScoreConfigRequest;
import com.example.taxi.security.AppRole;
import com.example.taxi.service.ScoreConfigService;
import com.example.taxi.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final ScoreConfigService scoreConfigService;

    public AdminController(UserService userService, ScoreConfigService scoreConfigService) {
        this.userService = userService;
        this.scoreConfigService = scoreConfigService;
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.listAll());
        model.addAttribute("roles", AppRole.values());
        return "admin/users";
    }

    @PostMapping("/users")
    public String createUser(@RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String displayName,
                             @RequestParam AppRole role,
                             RedirectAttributes ra) {
        userService.create(username, password, displayName, role);
        ra.addFlashAttribute("success", "Utilisateur « " + username + " » créé.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/role")
    public String updateRole(@PathVariable Long id, @RequestParam AppRole role, RedirectAttributes ra) {
        userService.updateRole(id, role);
        ra.addFlashAttribute("success", "Rôle mis à jour.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/enabled")
    public String setEnabled(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean enabled,
                             RedirectAttributes ra) {
        userService.setEnabled(id, enabled);
        ra.addFlashAttribute("success", "Compte " + (enabled ? "activé" : "désactivé") + ".");
        return "redirect:/admin/users";
    }

    @GetMapping("/config")
    public String config(Model model) {
        model.addAttribute("score", scoreConfigService.get());
        model.addAttribute("userCount", userService.listAll().size());
        return "admin/config";
    }

    @PostMapping("/config")
    public String saveConfig(@RequestParam BigDecimal loadWeight,
                             @RequestParam BigDecimal seniorityWeight,
                             @RequestParam BigDecimal vehicleWeight,
                             RedirectAttributes ra) {
        scoreConfigService.update(new ScoreConfigRequest(loadWeight, seniorityWeight, vehicleWeight));
        ra.addFlashAttribute("success", "Coefficients du score mis à jour.");
        return "redirect:/admin/config";
    }
}