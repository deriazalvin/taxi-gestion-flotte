package com.example.taxi.controller.rest;

import com.example.taxi.dto.request.ScoreConfigRequest;
import com.example.taxi.dto.request.UserRequest;
import com.example.taxi.dto.response.ScoreConfigResponse;
import com.example.taxi.dto.response.UserResponse;
import com.example.taxi.security.AppRole;
import com.example.taxi.service.ScoreConfigService;
import com.example.taxi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    private final UserService userService;
    private final ScoreConfigService scoreConfigService;

    public AdminRestController(UserService userService, ScoreConfigService scoreConfigService) {
        this.userService = userService;
        this.scoreConfigService = scoreConfigService;
    }

    @GetMapping("/users")
    public List<UserResponse> users() {
        return userService.listAll();
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(
                req.username(), req.password(), req.displayName(), req.role()));
    }

    @PatchMapping("/users/{id}/role")
    public UserResponse updateRole(@PathVariable Long id, @RequestBody AppRole role) {
        return userService.updateRole(id, role);
    }

    @PatchMapping("/users/{id}/enabled")
    public UserResponse setEnabled(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        userService.setEnabled(id, Boolean.TRUE.equals(body.get("enabled")));
        return userService.listAll().stream().filter(u -> u.id().equals(id)).findFirst().orElseThrow();
    }

    @GetMapping("/score-config")
    public ScoreConfigResponse getScoreConfig() {
        return scoreConfigService.get();
    }

    @PutMapping("/score-config")
    public ScoreConfigResponse updateScoreConfig(@Valid @RequestBody ScoreConfigRequest req) {
        return scoreConfigService.update(req);
    }
}