package com.example.taxi.config;

import com.example.taxi.dto.request.AssignRideRequest;
import com.example.taxi.dto.request.RideRequest;
import com.example.taxi.entity.AppUser;
import com.example.taxi.entity.Client;
import com.example.taxi.entity.Driver;
import com.example.taxi.entity.DriverStatus;
import com.example.taxi.entity.ScoreConfig;
import com.example.taxi.entity.Vehicle;
import com.example.taxi.repository.AppUserRepository;
import com.example.taxi.repository.ClientRepository;
import com.example.taxi.repository.DriverRepository;
import com.example.taxi.repository.ScoreConfigRepository;
import com.example.taxi.repository.VehicleRepository;
import com.example.taxi.security.AppRole;
import com.example.taxi.service.RideService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DemoDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

    private final AppUserRepository userRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final ClientRepository clientRepository;
    private final ScoreConfigRepository scoreConfigRepository;
    private final PasswordEncoder passwordEncoder;
    private final RideService rideService;
    private final String demoPassword;

    public DemoDataLoader(AppUserRepository userRepository,
                          DriverRepository driverRepository,
                          VehicleRepository vehicleRepository,
                          ClientRepository clientRepository,
                          ScoreConfigRepository scoreConfigRepository,
                          PasswordEncoder passwordEncoder,
                          RideService rideService,
                          @Value("${app.demo-data.password:password}") String demoPassword) {
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
        this.clientRepository = clientRepository;
        this.scoreConfigRepository = scoreConfigRepository;
        this.passwordEncoder = passwordEncoder;
        this.rideService = rideService;
        this.demoPassword = demoPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        seedUsers();
        seedScoreConfig();
        seedClients();
        seedFleet();
        seedRides();
        log.info("Donnees de demonstration chargees.");
    }

    private void seedUsers() {
        userRepository.save(new AppUser("admin", passwordEncoder.encode(demoPassword), "Administrateur", AppRole.ROLE_ADMIN));
        userRepository.save(new AppUser("manager", passwordEncoder.encode(demoPassword), "Responsable exploitation", AppRole.ROLE_MANAGER));
        userRepository.save(new AppUser("operator", passwordEncoder.encode(demoPassword), "Operateur radio", AppRole.ROLE_OPERATOR));
        userRepository.save(new AppUser("user", passwordEncoder.encode(demoPassword), "Consultant", AppRole.ROLE_USER));
    }

    private void seedScoreConfig() {
        if (scoreConfigRepository.count() == 0) {
            scoreConfigRepository.save(new ScoreConfig(new BigDecimal("2.00"),
                    new BigDecimal("0.50"), new BigDecimal("0.50")));
        }
    }

    private void seedClients() {
        if (clientRepository.count() == 0) {
            clientRepository.save(new Client("Atelier Lumiere", "07 10 20 30 40",
                    "contact@atelier-lumiere.fr", "12 rue des Artisans, Paris"));
            clientRepository.save(new Client("Sophie K.", "07 21 32 43 54",
                    "sophie.k@exemple.fr", "Place de la Republique, Lyon"));
            clientRepository.save(new Client("M. Bernard", "06 12 34 56 78",
                    null, "Rue de la Paix, Versailles"));
            clientRepository.save(new Client("Hotel Republique", "06 98 76 54 32",
                    "reservation@hotel-republique.fr", "Hotel de Ville, Nancy"));
        }
    }

    private void seedFleet() {
        Vehicle v1 = vehicleRepository.save(new Vehicle("AB-123-CD", "Toyota", "Prius", 2021, 4,
                new BigDecimal("45230.0")));
        Vehicle v2 = vehicleRepository.save(new Vehicle("EF-456-GH", "Mercedes", "Classe V", 2020, 7,
                new BigDecimal("89215.0")));
        Vehicle v3 = vehicleRepository.save(new Vehicle("IJ-789-KL", "Renault", "Trafic", 2023, 8,
                new BigDecimal("12340.0")));
        Vehicle v4 = vehicleRepository.save(new Vehicle("MN-000-OP", "Peugeot", "508", 2016, 4,
                new BigDecimal("120450.0")));
        vehicleRepository.save(new Vehicle("QR-111-ST", "Citroen", "C4", 2019, 4,
                new BigDecimal("77800.0")));
        vehicleRepository.save(new Vehicle("UV-222-WX", "Ford", "Transit", 2018, 9,
                new BigDecimal("151200.0")));

        driverRepository.save(driver("Marie", "Dupont", "06 11 22 33 44", "P-2020-11580",
                LocalDate.now().plusYears(2), LocalDate.now().minusYears(4), DriverStatus.DISPONIBLE,
                v1, new BigDecimal("8.00")));
        driverRepository.save(driver("Julien", "Martin", "06 22 33 44 55", "P-2019-11892",
                LocalDate.now().plusYears(1), LocalDate.now().minusYears(6), DriverStatus.DISPONIBLE,
                v2, new BigDecimal("10.00")));
        driverRepository.save(driver("Samira", "Benali", "06 33 44 55 66", "P-2021-22410",
                LocalDate.now().plusYears(3), LocalDate.now().minusYears(2), DriverStatus.EN_PAUSE,
                v3, new BigDecimal("12.00")));
        driverRepository.save(driver("Karim", "Haddad", "06 44 55 66 77", "P-2018-33517",
                LocalDate.now().plusYears(6), LocalDate.now().minusYears(8), DriverStatus.DISPONIBLE,
                v4, new BigDecimal("10.00")));
        driverRepository.save(driver("Lea", "Girard", "06 55 66 77 88", "P-2019-44901",
                LocalDate.now().plusYears(4), LocalDate.now().minusYears(1), DriverStatus.INDISPONIBLE,
                null, new BigDecimal("10.00")));
        driverRepository.save(driver("Antoine", "Roux", "06 66 77 88 99", "P-2022-55231",
                LocalDate.now().minusMonths(1), LocalDate.now().minusYears(1), DriverStatus.SUSPENDU,
                null, new BigDecimal("10.00")));
        driverRepository.save(driver("Claire", "Moreau", "06 77 88 99 00", "P-2021-66712",
                LocalDate.now().plusYears(5), LocalDate.now().minusYears(3), DriverStatus.DISPONIBLE,
                null, new BigDecimal("8.00")));
    }

    private Driver driver(String first, String last, String phone, String license,
                          LocalDate licenseExpiry, LocalDate hireDate, DriverStatus status,
                          Vehicle vehicle, BigDecimal commission) {
        Driver d = new Driver(first, last, phone, license, licenseExpiry, hireDate);
        d.setStatus(status);
        d.setVehicle(vehicle);
        d.setCommissionRate(commission);
        return d;
    }

    private void seedRides() {
        Long atelier = clientRepository.findByPhone("07 10 20 30 40").map(Client::getId).orElse(null);
        Long sophie = clientRepository.findByPhone("07 21 32 43 54").map(Client::getId).orElse(null);
        Long bernard = clientRepository.findByPhone("06 12 34 56 78").map(Client::getId).orElse(null);
        Long hotel = clientRepository.findByPhone("06 98 76 54 32").map(Client::getId).orElse(null);

        rideService.create(new RideRequest("Atelier Lumiere", "07 10 20 30 40",
                "Gare de Lyon, Paris", "Aeroport CDG, Terminal 2", new BigDecimal("35.0"),
                LocalDateTime.now().plusHours(2), new BigDecimal("65.00"), atelier));
        rideService.create(new RideRequest("Sophie K.", "07 21 32 43 54",
                "Place de la Republique, Lyon", "Zone commerciale Les Grands etats", new BigDecimal("12.5"),
                LocalDateTime.now().plusHours(3), new BigDecimal("28.00"), sophie));
        rideService.create(new RideRequest("M. Bernard", "06 12 34 56 78",
                "Rue de la Paix, Versailles", "Marina Yacht Club, Hyeres", new BigDecimal("112.0"),
                LocalDateTime.now().plusDays(1).withHour(8).withMinute(0), new BigDecimal("210.00"), bernard));

        // Une course deja terminee pour alimenter les statistiques (attribution auto)
        try {
            var created = rideService.create(new RideRequest("Demo INSEE", "06 98 76 54 32",
                    "Hotel de Ville, Nancy", "Centre des Congres, Nancy", new BigDecimal("6.0"),
                    LocalDateTime.now().minusDays(1).withHour(18).withMinute(30), new BigDecimal("18.00"), hotel));
            var assigned = rideService.assign(created.id(), new AssignRideRequest(null, null));
            if (assigned.status() == com.example.taxi.entity.RideStatus.ASSIGNEE) {
                rideService.start(created.id());
                rideService.complete(created.id());
            }
        } catch (Exception e) {
            log.warn("Course demo terminee non creee : {}", e.getMessage());
        }
    }
}