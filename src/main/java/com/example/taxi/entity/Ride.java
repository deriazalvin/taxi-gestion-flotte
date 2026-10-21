package com.example.taxi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Version;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rides")
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 30)
    @Column(nullable = false, unique = true)
    private String reference;

    @NotBlank
    @Size(max = 80)
    @Column(nullable = false)
    private String clientName;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false)
    private String clientPhone;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false)
    private String pickupLocation;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false)
    private String destination;

    @NotNull
    @DecimalMin("0.1")
    @Column(nullable = false, precision = 8, scale = 1)
    private BigDecimal distance;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime rideDateTime;

    @NotNull
    @DecimalMin("0.01")
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal fare;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RideStatus status = RideStatus.EN_ATTENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column(name = "assignment_score")
    private Integer assignmentScore;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;

    @Size(max = 500)
    private String cancelReason;

    @Version
    private Long version;

    protected Ride() {
    }

    public Ride(String reference, String clientName, String clientPhone, String pickupLocation,
                String destination, BigDecimal distance, LocalDateTime rideDateTime, BigDecimal fare) {
        this.reference = reference;
        this.clientName = clientName;
        this.clientPhone = clientPhone;
        this.pickupLocation = pickupLocation;
        this.destination = destination;
        this.distance = distance;
        this.rideDateTime = rideDateTime;
        this.fare = fare;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public String getClientPhone() { return clientPhone; }
    public void setClientPhone(String clientPhone) { this.clientPhone = clientPhone; }
    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public BigDecimal getDistance() { return distance; }
    public void setDistance(BigDecimal distance) { this.distance = distance; }
    public LocalDateTime getRideDateTime() { return rideDateTime; }
    public void setRideDateTime(LocalDateTime rideDateTime) { this.rideDateTime = rideDateTime; }
    public BigDecimal getFare() { return fare; }
    public void setFare(BigDecimal fare) { this.fare = fare; }
    public RideStatus getStatus() { return status; }
    public void setStatus(RideStatus status) { this.status = status; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    public Integer getAssignmentScore() { return assignmentScore; }
    public void setAssignmentScore(Integer assignmentScore) { this.assignmentScore = assignmentScore; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}