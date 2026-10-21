package com.example.taxi.service;

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
import com.example.taxi.repository.ClientRepository;
import com.example.taxi.repository.DriverRepository;
import com.example.taxi.repository.RideRepository;
import com.example.taxi.repository.ScoreConfigRepository;
import com.example.taxi.repository.VehicleRepository;
import com.example.taxi.service.impl.RideServiceImpl;
import com.example.taxi.service.impl.ScoreConfigServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RideServiceImplTest {

    @Mock
    private RideRepository rideRepository;
    @Mock
    private DriverRepository driverRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private ScoreConfigRepository scoreConfigRepository;

    private RideServiceImpl service;

    @BeforeEach
    void setUp() {
        ScoreConfig config = new ScoreConfig(new BigDecimal("2.00"),
                new BigDecimal("0.50"), new BigDecimal("0.50"));
        lenient().when(scoreConfigRepository.findAll())
                .thenReturn(List.of(config));
        ScoreConfigServiceImpl scoreConfigService = new ScoreConfigServiceImpl(scoreConfigRepository);
        service = new RideServiceImpl(rideRepository, driverRepository, vehicleRepository,
                clientRepository, scoreConfigService);
    }

    private Vehicle vehicle(String plate, int modelYear) {
        Vehicle v = new Vehicle(plate, "Marque", "Modele", modelYear, 4,
                new BigDecimal("50000"));
        v.setId((long) Math.abs(plate.hashCode()));
        v.setStatus(VehicleStatus.DISPONIBLE);
        return v;
    }

    private Driver driver(long id, String last, LocalDate hireDate, LocalDate licenseExpiry,
                          Vehicle vehicle) {
        Driver d = new Driver("Jean", last, "0600000000", "LIC-" + id,
                licenseExpiry, hireDate);
        d.setId(id);
        d.setStatus(DriverStatus.DISPONIBLE);
        d.setVehicle(vehicle);
        return d;
    }

    private Ride waitingRide(long id) {
        Ride ride = new Ride("CRS-TEST-" + id, "Client A", "0611111111", "Gare",
                "Aéroport", new BigDecimal("12.5"), LocalDateTime.now().plusHours(2),
                new BigDecimal("45.00"));
        ride.setId(id);
        ride.setStatus(RideStatus.EN_ATTENTE);
        return ride;
    }

    @Test
    void create_linksClientWhenClientIdProvided() {
        RideRequest req = new RideRequest("Client A", "0611111111", "Gare", "Aéroport",
                new BigDecimal("12.5"), LocalDateTime.now().plusHours(2),
                new BigDecimal("45.00"), 7L);
        Client client = new Client("Client A", "0611111111", "a@b.fr", "Paris");
        client.setId(7L);
        when(rideRepository.count()).thenReturn(0L);
        when(rideRepository.existsByReference(any())).thenReturn(false);
        when(clientRepository.findById(7L)).thenReturn(Optional.of(client));
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));

        RideResponse resp = service.create(req);

        assertThat(resp.clientId()).isEqualTo(7L);
        assertThat(resp.clientName()).isEqualTo("Client A");
        assertThat(resp.status()).isEqualTo(RideStatus.EN_ATTENTE);
    }

    @Test
    void create_withoutClientId_keepsFreeTextClient() {
        RideRequest req = new RideRequest("Client Libre", "0611111111", "Gare", "Aéroport",
                new BigDecimal("5.0"), LocalDateTime.now().plusHours(1),
                new BigDecimal("20.00"), null);
        when(rideRepository.count()).thenReturn(0L);
        when(rideRepository.existsByReference(any())).thenReturn(false);
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));

        RideResponse resp = service.create(req);

        assertThat(resp.clientId()).isNull();
        assertThat(resp.clientName()).isEqualTo("Client Libre");
    }

    @Test
    void autoAssign_picksDriverWithBestScore() {
        // Senior expérimenté (9/10) + véhicule ancien vs jeune chauffeur + véhicule neuf
        Vehicle oldCar = vehicle("AA-001", 2018);
        Vehicle newCar = vehicle("BB-002", 2025);
        Driver senior = driver(1, "Dupont", LocalDate.now().minusYears(9),
                LocalDate.now().plusYears(1), oldCar);
        Driver rookie = driver(2, "Martin", LocalDate.now().minusYears(1),
                LocalDate.now().plusYears(1), newCar);

        Ride ride = waitingRide(1);
        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));
        when(driverRepository.findByStatusOrdered(DriverStatus.DISPONIBLE))
                .thenReturn(List.of(senior, rookie));
        when(rideRepository.countByDriverIdAndStatusIn(anyLong(), anyList())).thenReturn(0L);
        when(rideRepository.countByVehicleIdAndStatusIn(anyLong(), anyList())).thenReturn(0L);
        when(rideRepository.countCompletedByDriverInRange(anyLong(), any(), any())).thenReturn(0L);
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));

        List<CandidateResponse> candidates = service.candidates(1L);
        assertThat(candidates).hasSize(2);
        assertThat(candidates.get(0).driverId()).isEqualTo(1L);
        assertThat(candidates.get(0).score())
                .isGreaterThan(candidates.get(1).score());

        RideResponse assigned = service.assign(1L, null);
        assertThat(assigned.status()).isEqualTo(RideStatus.ASSIGNEE);
        assertThat(assigned.driverId()).isEqualTo(1L);
        assertThat(senior.getStatus()).isEqualTo(DriverStatus.EN_COURSE);
        assertThat(oldCar.getStatus()).isEqualTo(VehicleStatus.EN_COURSE);
        assertThat(assigned.assignmentScore()).isNotNull();
    }

    @Test
    void autoAssign_rejectsWhenNoEligibleDriver() {
        Driver busy = driver(1, "Dupont", LocalDate.now().minusYears(5),
                LocalDate.now().plusYears(1), vehicle("AA-001", 2018));
        // Tout le monde a déjà une course active
        when(driverRepository.findByStatusOrdered(DriverStatus.DISPONIBLE))
                .thenReturn(List.of(busy));
        when(rideRepository.findById(1L)).thenReturn(Optional.of(waitingRide(1)));
        when(rideRepository.countByDriverIdAndStatusIn(anyLong(), anyList())).thenReturn(1L);

        assertThatThrownBy(() -> service.assign(1L, null))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void autoAssign_excludesDriverWithExpiredLicense() {
        Driver expired = driver(1, "Dupont", LocalDate.now().minusYears(5),
                LocalDate.now().minusDays(1), vehicle("AA-001", 2018));
        when(driverRepository.findByStatusOrdered(DriverStatus.DISPONIBLE))
                .thenReturn(List.of(expired));
        when(rideRepository.findById(1L)).thenReturn(Optional.of(waitingRide(1)));

        assertThat(service.candidates(1L)).isEmpty();
        assertThatThrownBy(() -> service.assign(1L, null))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void explicitAssign_validatesEligibilityAndVehicle() {
        Driver driver = driver(1, "Dupont", LocalDate.now().minusYears(5),
                LocalDate.now().plusYears(1), vehicle("AA-001", 2018));
        Ride ride = waitingRide(1);
        Ride ride2 = waitingRide(2);
        // 1er appel : véhicule refusé, 2e : attribution OK, 3e : nouvelle course en attente
        when(rideRepository.findById(1L))
                .thenReturn(Optional.of(ride), Optional.of(ride), Optional.of(ride2));
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(rideRepository.countByDriverIdAndStatusIn(anyLong(), anyList())).thenReturn(0L);
        when(rideRepository.countByVehicleIdAndStatusIn(anyLong(), anyList())).thenReturn(0L);
        when(rideRepository.countCompletedByDriverInRange(anyLong(), any(), any())).thenReturn(0L);
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));

        // Véhicule qui n'est pas le sien → refus
        assertThatThrownBy(() -> service.assign(1L, new AssignRideRequest(1L, 999L)))
                .isInstanceOf(InvalidOperationException.class);

        // Attribution correcte
        RideResponse resp = service.assign(1L, new AssignRideRequest(1L, driver.getVehicle().getId()));
        assertThat(resp.driverId()).isEqualTo(1L);
        assertThat(resp.vehicleId()).isEqualTo(driver.getVehicle().getId());
        assertThat(resp.status()).isEqualTo(RideStatus.ASSIGNEE);

        // Permis expiré → refus explicite
        Driver expired = driver(2, "Martin", LocalDate.now().minusYears(3),
                LocalDate.now().minusDays(2), vehicle("BB-002", 2020));
        when(driverRepository.findById(2L)).thenReturn(Optional.of(expired));
        assertThatThrownBy(() -> service.assign(1L, new AssignRideRequest(2L, null)))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void explicitAssign_rejectsDriverAlreadyOnActiveRide() {
        Driver driver = driver(1, "Dupont", LocalDate.now().minusYears(5),
                LocalDate.now().plusYears(1), vehicle("AA-001", 2018));
        when(rideRepository.findById(1L)).thenReturn(Optional.of(waitingRide(1)));
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(rideRepository.countByDriverIdAndStatusIn(anyLong(), anyList())).thenReturn(1L);

        assertThatThrownBy(() -> service.assign(1L, new AssignRideRequest(1L, null)))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void lifecycle_startCompleteAndCancel() {
        Ride ride = waitingRide(1);
        Driver driver = driver(1, "Dupont", LocalDate.now().minusYears(5),
                LocalDate.now().plusYears(1), vehicle("AA-001", 2018));

        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));

        // Un statut non assigné ne peut pas démarrer
        assertThatThrownBy(() -> service.start(1L)).isInstanceOf(InvalidOperationException.class);

        // Assignation puis démarrage
        ride.setDriver(driver);
        ride.setVehicle(driver.getVehicle());
        ride.setStatus(RideStatus.ASSIGNEE);
        RideResponse started = service.start(1L);
        assertThat(started.status()).isEqualTo(RideStatus.EN_COURS);
        assertThat(started.startedAt()).isNotNull();

        // Terminée → libération des ressources
        RideResponse completed = service.complete(1L);
        assertThat(completed.status()).isEqualTo(RideStatus.TERMINEE);
        assertThat(completed.completedAt()).isNotNull();
        assertThat(driver.getStatus()).isEqualTo(DriverStatus.DISPONIBLE);
        assertThat(driver.getVehicle().getStatus()).isEqualTo(VehicleStatus.DISPONIBLE);

        // Une course terminée ne peut plus être annulée
        assertThatThrownBy(() -> service.cancel(1L, "trop tard"))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void cancel_whileAssigned_freesResources() {
        Ride ride = waitingRide(1);
        Driver driver = driver(1, "Dupont", LocalDate.now().minusYears(5),
                LocalDate.now().plusYears(1), vehicle("AA-001", 2018));
        ride.setDriver(driver);
        ride.setVehicle(driver.getVehicle());
        ride.setStatus(RideStatus.ASSIGNEE);
        driver.setStatus(DriverStatus.EN_COURSE);
        ride.getVehicle().setStatus(VehicleStatus.EN_COURSE);

        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));

        RideResponse cancelled = service.cancel(1L, "client annule");
        assertThat(cancelled.status()).isEqualTo(RideStatus.ANNULEE);
        assertThat(cancelled.cancelReason()).isEqualTo("client annule");
        assertThat(driver.getStatus()).isEqualTo(DriverStatus.DISPONIBLE);
        assertThat(driver.getVehicle().getStatus()).isEqualTo(VehicleStatus.DISPONIBLE);
    }

    @Test
    void candidatesRejectedWhenRideNotWaiting() {
        Ride ride = waitingRide(1);
        ride.setStatus(RideStatus.ASSIGNEE);
        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));

        assertThatThrownBy(() -> service.candidates(1L))
                .isInstanceOf(InvalidOperationException.class);
    }
}