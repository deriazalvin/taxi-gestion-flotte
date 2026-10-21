package com.example.taxi.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Coefficients configurables du score d'attribution automatique des chauffeurs.
 * Paramètres : charge de travail (nombre de courses du jour), ancienneté, ancienneté du véhicule.
 */
@Entity
@Table(name = "score_config")
public class ScoreConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal loadWeight = new BigDecimal("2.00");

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal seniorityWeight = new BigDecimal("0.50");

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal vehicleWeight = new BigDecimal("0.50");

    protected ScoreConfig() {
    }

    public ScoreConfig(BigDecimal loadWeight, BigDecimal seniorityWeight, BigDecimal vehicleWeight) {
        this.loadWeight = loadWeight;
        this.seniorityWeight = seniorityWeight;
        this.vehicleWeight = vehicleWeight;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getLoadWeight() { return loadWeight; }
    public void setLoadWeight(BigDecimal loadWeight) { this.loadWeight = loadWeight; }
    public BigDecimal getSeniorityWeight() { return seniorityWeight; }
    public void setSeniorityWeight(BigDecimal seniorityWeight) { this.seniorityWeight = seniorityWeight; }
    public BigDecimal getVehicleWeight() { return vehicleWeight; }
    public void setVehicleWeight(BigDecimal vehicleWeight) { this.vehicleWeight = vehicleWeight; }
}