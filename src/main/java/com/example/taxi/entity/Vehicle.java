package com.example.taxi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, unique = true)
    private String plateNumber;

    @NotBlank
    @Size(max = 60)
    @Column(nullable = false)
    private String brand;

    @NotBlank
    @Size(max = 60)
    @Column(nullable = false)
    private String model;

    @NotNull
    @Min(1950)
    @Max(2100)
    @Column(nullable = false)
    private Integer modelYear;

    @NotNull
    @Min(1)
    @Max(20)
    @Column(nullable = false)
    private Integer capacity;

    @NotNull
    @DecimalMin("0")
    @Column(nullable = false, precision = 9, scale = 1)
    private BigDecimal mileage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleStatus status = VehicleStatus.DISPONIBLE;

    protected Vehicle() {
    }

    public Vehicle(String plateNumber, String brand, String model, Integer modelYear,
                   Integer capacity, BigDecimal mileage) {
        this.plateNumber = plateNumber;
        this.brand = brand;
        this.model = model;
        this.modelYear = modelYear;
        this.capacity = capacity;
        this.mileage = mileage;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Integer getModelYear() { return modelYear; }
    public void setModelYear(Integer modelYear) { this.modelYear = modelYear; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public BigDecimal getMileage() { return mileage; }
    public void setMileage(BigDecimal mileage) { this.mileage = mileage; }
    public VehicleStatus getStatus() { return status; }
    public void setStatus(VehicleStatus status) { this.status = status; }
}