package com.example.taxi.controller.rest;

import com.example.taxi.dto.request.VehicleRequest;
import com.example.taxi.dto.response.VehicleResponse;
import com.example.taxi.entity.VehicleStatus;
import com.example.taxi.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleRestController {

    private final VehicleService vehicleService;

    public VehicleRestController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public Page<VehicleResponse> list(@RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) VehicleStatus status,
                                      @PageableDefault(size = 10) Pageable pageable) {
        return vehicleService.search(keyword, status, pageable);
    }

    @GetMapping("/available")
    public List<VehicleResponse> available() {
        return vehicleService.listAvailable();
    }

    @GetMapping("/{id}")
    public VehicleResponse get(@PathVariable Long id) {
        return vehicleService.getById(id);
    }

    @PostMapping
    public ResponseEntity<VehicleResponse> create(@Valid @RequestBody VehicleRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.create(req));
    }

    @PutMapping("/{id}")
    public VehicleResponse update(@PathVariable Long id, @Valid @RequestBody VehicleRequest req) {
        return vehicleService.update(id, req);
    }

    @PatchMapping("/{id}/status")
    public VehicleResponse setStatus(@PathVariable Long id, @RequestBody VehicleStatus status) {
        return vehicleService.setStatus(id, status);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        vehicleService.delete(id);
    }
}