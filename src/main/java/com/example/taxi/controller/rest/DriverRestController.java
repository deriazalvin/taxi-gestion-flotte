package com.example.taxi.controller.rest;

import com.example.taxi.dto.request.DriverRequest;
import com.example.taxi.dto.response.DriverResponse;
import com.example.taxi.entity.DriverStatus;
import com.example.taxi.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drivers")
public class DriverRestController {

    private final DriverService driverService;

    public DriverRestController(DriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping
    public Page<DriverResponse> list(@RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) DriverStatus status,
                                     @PageableDefault(size = 10) Pageable pageable) {
        return driverService.search(keyword, status, pageable);
    }

    @GetMapping("/{id}")
    public DriverResponse get(@PathVariable Long id) {
        return driverService.getById(id);
    }

    @PostMapping
    public ResponseEntity<DriverResponse> create(@Valid @RequestBody DriverRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(driverService.create(req));
    }

    @PutMapping("/{id}")
    public DriverResponse update(@PathVariable Long id, @Valid @RequestBody DriverRequest req) {
        return driverService.update(id, req);
    }

    @PatchMapping("/{id}/status")
    public DriverResponse setStatus(@PathVariable Long id, @RequestBody DriverStatus status) {
        return driverService.setStatus(id, status);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        driverService.delete(id);
    }
}