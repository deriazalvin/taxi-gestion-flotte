package com.example.taxi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "drivers")
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 60)
    @Column(nullable = false)
    private String firstName;

    @NotBlank
    @Size(max = 60)
    @Column(nullable = false)
    private String lastName;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false)
    private String phone;

    @NotBlank
    @Size(max = 30)
    @Column(nullable = false, unique = true)
    private String licenseNumber;

    @NotNull
    @Column(nullable = false)
    private LocalDate licenseExpiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DriverStatus status = DriverStatus.DISPONIBLE;

    @NotNull
    @Column(nullable = false)
    private LocalDate hireDate;

    /**
     * Commission (en %) prélevée sur le tarif de chaque course. Le reste revient au chauffeur.
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate = new BigDecimal("10.00");

    /** Véhicule associé au chauffeur (obligatoire pour recevoir une course). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    protected Driver() {
    }

    public Driver(String firstName, String lastName, String phone, String licenseNumber,
                  LocalDate licenseExpiryDate, LocalDate hireDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.licenseNumber = licenseNumber;
        this.licenseExpiryDate = licenseExpiryDate;
        this.hireDate = hireDate;
    }

    public boolean hasValidLicense() {
        return licenseExpiryDate != null && !licenseExpiryDate.isBefore(LocalDate.now());
    }

    public String fullName() {
        return firstName + " " + lastName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public LocalDate getLicenseExpiryDate() { return licenseExpiryDate; }
    public void setLicenseExpiryDate(LocalDate licenseExpiryDate) { this.licenseExpiryDate = licenseExpiryDate; }
    public DriverStatus getStatus() { return status; }
    public void setStatus(DriverStatus status) { this.status = status; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
}