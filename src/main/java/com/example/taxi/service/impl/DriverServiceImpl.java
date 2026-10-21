package com.example.taxi.service.impl;

import com.example.taxi.dto.request.DriverRequest;
import com.example.taxi.dto.response.DriverResponse;
import com.example.taxi.entity.Driver;
import com.example.taxi.entity.DriverStatus;
import com.example.taxi.entity.Ride;
import com.example.taxi.entity.RideStatus;
import com.example.taxi.entity.Vehicle;
import com.example.taxi.exception.ConflictException;
import com.example.taxi.exception.InvalidOperationException;
import com.example.taxi.exception.ResourceNotFoundException;
import com.example.taxi.mapper.DriverMapper;
import com.example.taxi.repository.DriverRepository;
import com.example.taxi.repository.RideRepository;
import com.example.taxi.repository.VehicleRepository;
import com.example.taxi.service.DriverService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final RideRepository rideRepository;

    public DriverServiceImpl(DriverRepository driverRepository,
                             VehicleRepository vehicleRepository,
                             RideRepository rideRepository) {
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
        this.rideRepository = rideRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DriverResponse> search(String keyword, DriverStatus status, Pageable pageable) {
        return driverRepository.findAll((root, q, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (keyword != null && !keyword.isBlank()) {
                String like = keyword.toLowerCase();
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), "%" + like + "%"),
                        cb.like(cb.lower(root.get("lastName")), "%" + like + "%"),
                        cb.like(cb.lower(root.get("phone")), "%" + like + "%"),
                        cb.like(cb.lower(root.get("licenseNumber")), "%" + like + "%")));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        }, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverResponse> listAll() {
        return driverRepository.findAll().stream().map(DriverResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DriverResponse getById(Long id) {
        Driver driver = findEntity(id);
        DriverResponse base = DriverResponse.from(driver);
        long ridesToday = rideRepository.countCompletedByDriverInRange(
                driver.getId(), LocalDate.now().atStartOfDay(), LocalDate.now().plusDays(1).atStartOfDay());
        Long activeRideId = rideRepository.findFirstByDriverIdAndStatusIn(
                        driver.getId(), List.of(RideStatus.ASSIGNEE, RideStatus.EN_COURS))
                .map(Ride::getId).orElse(null);
        return new DriverResponse(base.id(), base.firstName(), base.lastName(), base.phone(),
                base.licenseNumber(), base.licenseExpiryDate(), base.licenseValid(), base.status(),
                base.hireDate(), base.commissionRate(), base.vehicleId(), base.vehiclePlate(),
                activeRideId, (int) ridesToday);
    }

    @Override
    @Transactional
    public DriverResponse create(DriverRequest req) {
        if (driverRepository.findByLicenseNumberIgnoreCase(req.licenseNumber()).isPresent()) {
            throw new ConflictException("Un chauffeur possède déjà le permis « " + req.licenseNumber() + " ».");
        }
        Driver driver = DriverMapper.toEntity(req);
        attachVehicle(driver, req.vehicleId());
        return DriverResponse.from(driverRepository.save(driver));
    }

    @Override
    @Transactional
    public DriverResponse update(Long id, DriverRequest req) {
        Driver driver = findEntity(id);
        driverRepository.findByLicenseNumberIgnoreCaseExcluding(req.licenseNumber(), id)
                .ifPresent(other -> {
                    throw new ConflictException("Un chauffeur possède déjà le permis « " + req.licenseNumber() + " ».");
                });
        if (hasActiveRide(id) && !sameVehicle(driver, req.vehicleId())) {
            throw new InvalidOperationException(
                    "Ce chauffeur est en course : impossible de changer son véhicule tant que la course est active.");
        }
        DriverMapper.update(driver, req);
        attachVehicle(driver, req.vehicleId());
        return DriverResponse.from(driverRepository.save(driver));
    }

    @Override
    @Transactional
    public DriverResponse setStatus(Long id, DriverStatus status) {
        Driver driver = findEntity(id);
        if ((status == DriverStatus.DISPONIBLE && hasActiveRide(id))) {
            throw new InvalidOperationException(
                    "Ce chauffeur a une course en cours : il ne peut pas être « disponible ».");
        }
        driver.setStatus(status);
        return DriverResponse.from(driverRepository.save(driver));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Driver driver = findEntity(id);
        if (rideRepository.countByDriverId(id) > 0) {
            throw new ConflictException("Impossible de supprimer ce chauffeur : l'historique de courses existe.");
        }
        driverRepository.delete(driver);
    }

    @Transactional(readOnly = true)
    public Driver findEntity(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chauffeur introuvable : id " + id));
    }

    private void attachVehicle(Driver driver, Long vehicleId) {
        if (vehicleId != null) {
            Vehicle vehicle = vehicleRepository.findById(vehicleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable : id " + vehicleId));
            driver.setVehicle(vehicle);
        } else {
            driver.setVehicle(null);
        }
    }

    private boolean sameVehicle(Driver driver, Long vehicleId) {
        if (vehicleId == null) {
            return driver.getVehicle() == null;
        }
        return driver.getVehicle() != null && driver.getVehicle().getId().equals(vehicleId);
    }

    private boolean hasActiveRide(Long driverId) {
        return rideRepository.countByDriverIdAndStatusIn(driverId,
                List.of(RideStatus.ASSIGNEE, RideStatus.EN_COURS)) > 0;
    }

    private DriverResponse toResponse(Driver d) {
        return DriverResponse.from(d);
    }
}