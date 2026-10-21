package com.example.taxi.repository;

import com.example.taxi.entity.Ride;
import com.example.taxi.entity.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RideRepository extends JpaRepository<Ride, Long>, JpaSpecificationExecutor<Ride> {

    boolean existsByReference(String reference);

    long countByStatus(RideStatus status);

    long countByDriverIdAndStatusIn(Long driverId, List<RideStatus> statuses);

    long countByVehicleIdAndStatusIn(Long vehicleId, List<RideStatus> statuses);

    Optional<Ride> findFirstByDriverIdAndStatusIn(Long driverId, List<RideStatus> statuses);

    Optional<Ride> findFirstByVehicleIdAndStatusIn(Long vehicleId, List<RideStatus> statuses);

    long countByDriverIdAndStatus(Long driverId, RideStatus status);

    long countByVehicleIdAndStatus(Long vehicleId, RideStatus status);

    long countByDriverId(Long driverId);

    long countByVehicleId(Long vehicleId);

    long countByClientId(Long clientId);

    @Query("select count(r) from Ride r where r.status = com.example.taxi.entity.RideStatus.ANNULEE " +
            "and r.cancelledAt >= :from and r.cancelledAt < :to")
    long countCanceledBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("select r from Ride r where r.status = com.example.taxi.entity.RideStatus.TERMINEE " +
            "and r.completedAt >= :from and r.completedAt < :to")
    List<Ride> findCompletedInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("select count(r) from Ride r where r.status = com.example.taxi.entity.RideStatus.TERMINEE " +
            "and r.completedAt >= :from and r.completedAt < :to")
    long countCompletedInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("select count(r) from Ride r where r.status = com.example.taxi.entity.RideStatus.ANNULEE")
    long countCanceled();

    @Query("select r from Ride r where r.status = com.example.taxi.entity.RideStatus.EN_ATTENTE " +
            "order by r.rideDateTime asc")
    List<Ride> findWaiting();

    @Query("select count(r) from Ride r where r.driver.id = :driverId and r.status = com.example.taxi.entity.RideStatus.TERMINEE " +
            "and r.completedAt >= :from and r.completedAt < :to")
    long countCompletedByDriverInRange(@Param("driverId") Long driverId,
                                       @Param("from") LocalDateTime from,
                                       @Param("to") LocalDateTime to);
}