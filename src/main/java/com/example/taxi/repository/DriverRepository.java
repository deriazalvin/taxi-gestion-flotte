package com.example.taxi.repository;

import com.example.taxi.entity.Driver;
import com.example.taxi.entity.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long>, JpaSpecificationExecutor<Driver> {

    Optional<Driver> findByLicenseNumberIgnoreCase(String licenseNumber);

    long countByLicenseExpiryDateBefore(LocalDate date);

    long countByStatus(DriverStatus status);

    @Query("select d from Driver d where d.licenseNumber = :license and d.id <> :id")
    Optional<Driver> findByLicenseNumberIgnoreCaseExcluding(@Param("license") String license, @Param("id") Long id);

    @Query("select d from Driver d where d.status = :status order by d.lastName asc, d.firstName asc")
    List<Driver> findByStatusOrdered(@Param("status") DriverStatus status);
}