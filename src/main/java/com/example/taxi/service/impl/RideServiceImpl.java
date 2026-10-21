package com.example.taxi.service.impl;

import com.example.taxi.dto.request.AssignRideRequest;
import com.example.taxi.dto.request.RideRequest;
import com.example.taxi.dto.response.CandidateResponse;
import com.example.taxi.dto.response.RideResponse;
import com.example.taxi.entity.Client;
import com.example.taxi.entity.Driver;
import com.example.taxi.entity.DriverStatus;
import com.example.taxi.entity.Ride;
import com.example.taxi.entity.RideStatus;
import com.example.taxi.entity.ScoreConfig;
import com.example.taxi.entity.Vehicle;
import com.example.taxi.entity.VehicleStatus;
import com.example.taxi.exception.InvalidOperationException;
import com.example.taxi.exception.ResourceNotFoundException;
import com.example.taxi.mapper.RideMapper;
import com.example.taxi.repository.ClientRepository;
import com.example.taxi.repository.DriverRepository;
import com.example.taxi.repository.RideRepository;
import com.example.taxi.repository.VehicleRepository;
import com.example.taxi.service.RideService;
import com.example.taxi.util.RideNumberGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RideServiceImpl implements RideService {

    private static final List<RideStatus> ACTIVE_STATUSES = List.of(RideStatus.ASSIGNEE, RideStatus.EN_COURS);

    private final RideRepository rideRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final ClientRepository clientRepository;
    private final ScoreConfigServiceImpl scoreConfigService;

    public RideServiceImpl(RideRepository rideRepository,
                           DriverRepository driverRepository,
                           VehicleRepository vehicleRepository,
                           ClientRepository clientRepository,
                           ScoreConfigServiceImpl scoreConfigService) {
        this.rideRepository = rideRepository;
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
        this.clientRepository = clientRepository;
        this.scoreConfigService = scoreConfigService;
    }

    // ------------------------------------------------------------------
    // Lecture / recherche
    // ------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Page<RideResponse> search(String keyword, RideStatus status, Pageable pageable) {
        return rideRepository.findAll((root, q, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (keyword != null && !keyword.isBlank()) {
                String like = keyword.toLowerCase();
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("reference")), "%" + like + "%"),
                        cb.like(cb.lower(root.get("clientName")), "%" + like + "%"),
                        cb.like(cb.lower(root.get("pickupLocation")), "%" + like + "%"),
                        cb.like(cb.lower(root.get("destination")), "%" + like + "%")));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        }, pageable).map(RideResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public RideResponse getById(Long id) {
        return RideResponse.from(findEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RideResponse> listWaiting() {
        return rideRepository.findWaiting().stream().map(RideResponse::from).toList();
    }

    // ------------------------------------------------------------------
    // Création
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public RideResponse create(RideRequest req) {
        Ride ride = RideMapper.toEntity(req, nextReference());
        if (req.clientId() != null) {
            Client client = clientRepository.findById(req.clientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client introuvable : id " + req.clientId()));
            ride.setClient(client);
            ride.setClientName(client.getName());
            ride.setClientPhone(client.getPhone());
        }
        return RideResponse.from(rideRepository.save(ride));
    }

    private String nextReference() {
        long count = rideRepository.count();
        String candidate;
        int attempts = 0;
        do {
            candidate = RideNumberGenerator.next(count + attempts);
            attempts++;
        } while (rideRepository.existsByReference(candidate) && attempts < 100);
        return candidate;
    }

    // ------------------------------------------------------------------
    // Attribution automatique (score)
    // ------------------------------------------------------------------

    /**
     * Candidats éligibles classés par score décroissant pour une course donnée.
     */
    @Override
    @Transactional(readOnly = true)
    public List<CandidateResponse> candidates(Long rideId) {
        Ride ride = findEntity(rideId);
        if (ride.getStatus() != RideStatus.EN_ATTENTE) {
            throw new InvalidOperationException(
                    "La course « " + ride.getReference() + " » n'est plus en attente : aucune attribution possible.");
        }
        List<ScoredDriver> scored = scoredCandidates(ride);
        return scored.stream().map(s -> toCandidate(s, ride)).toList();
    }

    @Override
    @Transactional
    public RideResponse assign(Long rideId, AssignRideRequest req) {
        Ride ride = findEntity(rideId);
        if (ride.getStatus() != RideStatus.EN_ATTENTE) {
            throw new InvalidOperationException(
                    "La course « " + ride.getReference() + " » n'est plus en attente : attribuez-la avant tout démarrage.");
        }

        if (req != null && req.driverId() != null) {
            assignExplicit(ride, req);
        } else {
            List<ScoredDriver> scored = scoredCandidates(ride);
            if (scored.isEmpty()) {
                throw new InvalidOperationException(
                        "Aucun chauffeur éligible : vérifiez les disponibilités, les permis et les véhicules associés.");
            }
            ScoredDriver best = scored.get(0);
            assignRide(ride, best.driver(), best.totalScore());
        }
        return RideResponse.from(rideRepository.save(ride));
    }

    private void assignExplicit(Ride ride, AssignRideRequest req) {
        Driver driver = driverRepository.findById(req.driverId())
                .orElseThrow(() -> new ResourceNotFoundException("Chauffeur introuvable : id " + req.driverId()));
        validateDriverEligible(ride, driver);
        if (req.vehicleId() != null && !req.vehicleId().equals(driver.getVehicle().getId())) {
            throw new InvalidOperationException(
                    "Le véhicule demandé n'est pas le véhicule associé au chauffeur « " + driver.fullName() + " ».");
        }
        ScoredDriver scored = score(driver);
        assignRide(ride, driver, scored.totalScore());
    }

    private void assignRide(Ride ride, Driver driver, BigDecimal score) {
        ride.setDriver(driver);
        ride.setVehicle(driver.getVehicle());
        ride.setStatus(RideStatus.ASSIGNEE);
        ride.setAssignmentScore(score.setScale(0, RoundingMode.HALF_UP).intValue());
        driver.setStatus(DriverStatus.EN_COURSE);
        driver.getVehicle().setStatus(VehicleStatus.EN_COURSE);
        driverRepository.save(driver);
        vehicleRepository.save(driver.getVehicle());
    }

    /**
     * Chauffeurs éligibles pondérés par le score configurable.
     */
    private List<ScoredDriver> scoredCandidates(Ride ride) {
        ScoreConfig config = scoreConfigService.current();
        List<Driver> eligible = driverRepository.findByStatusOrdered(DriverStatus.DISPONIBLE).stream()
                .filter(d -> isEligible(ride, d))
                .toList();

        List<ScoredDriver> scored = eligible.stream().map(this::score).toList();
        long maxLoad = scored.stream().mapToLong(ScoredDriver::load).max().orElse(0);
        scored = scored.stream().map(s -> withScore(s, maxLoad, config)).toList();

        return scored.stream()
                .sorted(Comparator.comparing(ScoredDriver::totalScore).reversed()
                        .thenComparing(ScoredDriver::load)
                        .thenComparing(s -> s.driver().getId()))
                .toList();
    }

    private boolean isEligible(Ride ride, Driver driver) {
        if (driver.getStatus() != DriverStatus.DISPONIBLE) {
            return false;
        }
        if (!driver.hasValidLicense()) {
            return false;
        }
        if (driver.getVehicle() == null || driver.getVehicle().getStatus() != VehicleStatus.DISPONIBLE) {
            return false;
        }
        if (rideRepository.countByDriverIdAndStatusIn(driver.getId(), ACTIVE_STATUSES) > 0) {
            return false;
        }
        if (rideRepository.countByVehicleIdAndStatusIn(driver.getVehicle().getId(), ACTIVE_STATUSES) > 0) {
            return false;
        }
        return true;
    }

    private void validateDriverEligible(Ride ride, Driver driver) {
        if (!driver.hasValidLicense()) {
            throw new InvalidOperationException(
                    "Le permis de « " + driver.fullName() + " » est expiré : attribution refusée.");
        }
        if (driver.getStatus() != DriverStatus.DISPONIBLE) {
            throw new InvalidOperationException(
                    "« " + driver.fullName() + " » n'est pas disponible (statut : " + driver.getStatus() + ").");
        }
        if (driver.getVehicle() == null || driver.getVehicle().getStatus() != VehicleStatus.DISPONIBLE) {
            throw new InvalidOperationException(
                    "« " + driver.fullName() + " » n'est pas associé à un véhicule disponible.");
        }
        if (rideRepository.countByDriverIdAndStatusIn(driver.getId(), ACTIVE_STATUSES) > 0) {
            throw new InvalidOperationException(
                    "« " + driver.fullName() + " » a déjà une course en cours : attribution refusée.");
        }
        if (rideRepository.countByVehicleIdAndStatusIn(driver.getVehicle().getId(), ACTIVE_STATUSES) > 0) {
            throw new InvalidOperationException(
                    "Le véhicule « " + driver.getVehicle().getPlateNumber() + " » est déjà en course.");
        }
    }

    private ScoredDriver score(Driver driver) {
        LocalDate today = LocalDate.now();
        long load = rideRepository.countCompletedByDriverInRange(
                driver.getId(), today.atStartOfDay(), today.plusDays(1).atStartOfDay());

        ScoreConfig config = scoreConfigService.current();
        BigDecimal seniorityPoints = seniorityPoints(driver);
        BigDecimal vehiclePoints = vehiclePoints(driver.getVehicle());
        return withScore(new ScoredDriver(driver, load, null, seniorityPoints, vehiclePoints, null),
                Math.max(load, 1L), config);
    }

    private ScoredDriver withScore(ScoredDriver s, long maxLoad, ScoreConfig config) {
        BigDecimal loadPoints = loadPoints(s.load(), maxLoad);
        BigDecimal total = config.getLoadWeight().multiply(loadPoints)
                .add(config.getSeniorityWeight().multiply(s.seniorityPoints()))
                .add(config.getVehicleWeight().multiply(s.vehiclePoints()))
                .setScale(2, RoundingMode.HALF_UP);
        return new ScoredDriver(s.driver(), s.load(), loadPoints, s.seniorityPoints(), s.vehiclePoints(), total);
    }

    private BigDecimal loadPoints(long load, long maxLoad) {
        double pct = maxLoad > 0 ? (double) load / maxLoad : 0.0;
        return BigDecimal.valueOf(10.0 - 10.0 * pct).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal seniorityPoints(Driver driver) {
        long days = ChronoUnit.DAYS.between(driver.getHireDate(), LocalDate.now());
        double years = Math.max(0, days / 365.0);
        return BigDecimal.valueOf(Math.min(10, years)).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal vehiclePoints(Vehicle vehicle) {
        int age = LocalDate.now().getYear() - vehicle.getModelYear();
        return BigDecimal.valueOf(Math.max(0, Math.min(10, age))).setScale(2, RoundingMode.HALF_UP);
    }

    private CandidateResponse toCandidate(ScoredDriver s, Ride ride) {
        return new CandidateResponse(s.driver().getId(), s.driver().fullName(),
                s.driver().getVehicle().getPlateNumber(), s.driver().getVehicle().getCapacity(),
                s.totalScore(), s.loadPoints(), s.seniorityPoints(), s.vehiclePoints());
    }

    // ------------------------------------------------------------------
    // Cycle de vie
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public RideResponse start(Long rideId) {
        Ride ride = findEntity(rideId);
        if (ride.getStatus() != RideStatus.ASSIGNEE) {
            throw new InvalidOperationException("La course doit être « assignée » avant de démarrer (statut : " + ride.getStatus() + ").");
        }
        ride.setStatus(RideStatus.EN_COURS);
        ride.setStartedAt(LocalDateTime.now());
        return RideResponse.from(rideRepository.save(ride));
    }

    @Override
    @Transactional
    public RideResponse complete(Long rideId) {
        Ride ride = findEntity(rideId);
        if (ride.getStatus() != RideStatus.EN_COURS) {
            throw new InvalidOperationException("La course doit être « en cours » pour être terminée (statut : " + ride.getStatus() + ").");
        }
        ride.setStatus(RideStatus.TERMINEE);
        ride.setCompletedAt(LocalDateTime.now());
        freeResources(ride);
        return RideResponse.from(rideRepository.save(ride));
    }

    @Override
    @Transactional
    public RideResponse cancel(Long rideId, String reason) {
        Ride ride = findEntity(rideId);
        if (ride.getStatus() == RideStatus.TERMINEE) {
            throw new InvalidOperationException("Une course terminée ne peut pas être annulée.");
        }
        if (ride.getStatus() == RideStatus.ANNULEE) {
            throw new InvalidOperationException("Cette course est déjà annulée.");
        }
        if (ride.getStatus() == RideStatus.ASSIGNEE || ride.getStatus() == RideStatus.EN_COURS) {
            freeResources(ride);
        }
        ride.setStatus(RideStatus.ANNULEE);
        ride.setCancelledAt(LocalDateTime.now());
        ride.setCancelReason(reason);
        return RideResponse.from(rideRepository.save(ride));
    }

    private void freeResources(Ride ride) {
        if (ride.getDriver() != null) {
            ride.getDriver().setStatus(DriverStatus.DISPONIBLE);
            driverRepository.save(ride.getDriver());
        }
        if (ride.getVehicle() != null) {
            ride.getVehicle().setStatus(VehicleStatus.DISPONIBLE);
            vehicleRepository.save(ride.getVehicle());
        }
    }

    @Transactional(readOnly = true)
    public Ride findEntity(Long id) {
        return rideRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course introuvable : id " + id));
    }

    private record ScoredDriver(Driver driver, long load, BigDecimal loadPoints,
                                BigDecimal seniorityPoints, BigDecimal vehiclePoints, BigDecimal totalScore) {
    }
}