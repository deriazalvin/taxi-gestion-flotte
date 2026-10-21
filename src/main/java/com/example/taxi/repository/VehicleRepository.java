package com.example.taxi.repository;

import com.example.taxi.entity.Vehicle;
import com.example.taxi.entity.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {

    Optional<Vehicle> findByPlateNumberIgnoreCase(String plateNumber);

    long countByStatus(VehicleStatus status);

    @Query("select v from Vehicle v where v.plateNumber = :plate and v.id <> :id")
    Optional<Vehicle> findByPlateNumberIgnoreCaseExcluding(@Param("plate") String plate, @Param("id") Long id);
}