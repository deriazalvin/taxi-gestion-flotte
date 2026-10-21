package com.example.taxi.controller.rest;

import com.example.taxi.dto.request.AssignRideRequest;
import com.example.taxi.dto.request.RideRequest;
import com.example.taxi.dto.response.CandidateResponse;
import com.example.taxi.dto.response.RideResponse;
import com.example.taxi.entity.RideStatus;
import com.example.taxi.service.RideService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rides")
public class RideRestController {

    private final RideService rideService;

    public RideRestController(RideService rideService) {
        this.rideService = rideService;
    }

    @GetMapping
    public Page<RideResponse> list(@RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) RideStatus status,
                                   @PageableDefault(size = 10) Pageable pageable) {
        return rideService.search(keyword, status, pageable);
    }

    @GetMapping("/waiting")
    public List<RideResponse> waiting() {
        return rideService.listWaiting();
    }

    @GetMapping("/{id}")
    public RideResponse get(@PathVariable Long id) {
        return rideService.getById(id);
    }

    @GetMapping("/{id}/candidates")
    public List<CandidateResponse> candidates(@PathVariable Long id) {
        return rideService.candidates(id);
    }

    @PostMapping
    public ResponseEntity<RideResponse> create(@Valid @RequestBody RideRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rideService.create(req));
    }

    @PostMapping("/{id}/assign")
    public RideResponse assign(@PathVariable Long id,
                               @Valid @RequestBody(required = false) AssignRideRequest req) {
        return rideService.assign(id, req);
    }

    @PostMapping("/{id}/start")
    public RideResponse start(@PathVariable Long id) {
        return rideService.start(id);
    }

    @PostMapping("/{id}/complete")
    public RideResponse complete(@PathVariable Long id) {
        return rideService.complete(id);
    }

    @PostMapping("/{id}/cancel")
    public RideResponse cancel(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        return rideService.cancel(id, body != null ? body.get("reason") : null);
    }
}