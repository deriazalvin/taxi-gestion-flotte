package com.example.taxi.service.impl;

import com.example.taxi.dto.request.VehicleRequest;
import com.example.taxi.dto.response.VehicleResponse;
import com.example.taxi.entity.RideStatus;
import com.example.taxi.entity.Vehicle;
import com.example.taxi.entity.VehicleStatus;
import com.example.taxi.exception.ConflictException;
import com.example.taxi.exception.InvalidOperationException;
import com.example.taxi.exception.ResourceNotFoundException;
import com.example.taxi.mapper.VehicleMapper;
import com.example.taxi.repository.RideRepository;
import com.example.taxi.repository.VehicleRepository;
import com.example.taxi.service.VehicleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final RideRepository rideRepository;

    public VehicleServiceImpl(VehicleRepository vehicleRepository, RideRepository rideRepository) {
        this.vehicleRepository = vehicleRepository;
        this.rideRepository = rideRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehicleResponse> search(String keyword, VehicleStatus status, Pageable pageable) {
        return vehicleRepository.findAll((root, q, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (keyword != null && !keyword.isBlank()) {
                String like = keyword.toLowerCase();
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("plateNumber")), "%" + like + "%"),
                        cb.like(cb.lower(root.get("brand")), "%" + like + "%"),
                        cb.like(cb.lower(root.get("model")), "%" + like + "%")));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        }, pageable).map(VehicleResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> listAll() {
        return vehicleRepository.findAll().stream().map(VehicleResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> listAvailable() {
        return vehicleRepository.findAll().stream()
                .filter(v -> v.getStatus() == VehicleStatus.DISPONIBLE)
                .map(VehicleResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getById(Long id) {
        return VehicleResponse.from(findEntity(id));
    }

    @Override
    @Transactional
    public VehicleResponse create(VehicleRequest req) {
        if (vehicleRepository.findByPlateNumberIgnoreCase(req.plateNumber()).isPresent()) {
            throw new ConflictException("Un véhicule possède déjà l'immatriculation « " + req.plateNumber() + " ».");
        }
        return VehicleResponse.from(vehicleRepository.save(VehicleMapper.toEntity(req)));
    }

    @Override
    @Transactional
    public VehicleResponse update(Long id, VehicleRequest req) {
        Vehicle vehicle = findEntity(id);
        vehicleRepository.findByPlateNumberIgnoreCaseExcluding(req.plateNumber(), id)
                .ifPresent(other -> {
                    throw new ConflictException("Un véhicule possède déjà l'immatriculation « " + req.plateNumber() + " ».");
                });
        VehicleMapper.update(vehicle, req);
        return VehicleResponse.from(vehicleRepository.save(vehicle));
    }

    @Override
    @Transactional
    public VehicleResponse setStatus(Long id, VehicleStatus status) {
        Vehicle vehicle = findEntity(id);
        if (status == VehicleStatus.DISPONIBLE && hasActiveRide(id)) {
            throw new InvalidOperationException("Ce véhicule est en course : il ne peut pas être « disponible ».");
        }
        if ((status == VehicleStatus.MAINTENANCE || status == VehicleStatus.HORS_SERVICE) && hasActiveRide(id)) {
            throw new InvalidOperationException(
                    "Ce véhicule a une course en cours : impossible de passer en maintenance actuellement.");
        }
        vehicle.setStatus(status);
        return VehicleResponse.from(vehicleRepository.save(vehicle));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Vehicle vehicle = findEntity(id);
        if (rideRepository.countByVehicleId(id) > 0) {
            throw new ConflictException("Impossible de supprimer ce véhicule : l'historique de courses existe.");
        }
        vehicleRepository.delete(vehicle);
    }

    @Transactional(readOnly = true)
    public Vehicle findEntity(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable : id " + id));
    }

    private boolean hasActiveRide(Long vehicleId) {
        return rideRepository.countByVehicleIdAndStatusIn(vehicleId,
                List.of(RideStatus.ASSIGNEE, RideStatus.EN_COURS)) > 0;
    }
}