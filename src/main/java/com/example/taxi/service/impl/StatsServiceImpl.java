package com.example.taxi.service.impl;

import com.example.taxi.dto.response.ActivityByDayResponse;
import com.example.taxi.dto.response.ActivityStatsResponse;
import com.example.taxi.dto.response.RevenueBreakdownResponse;
import com.example.taxi.dto.response.RevenueByDayResponse;
import com.example.taxi.dto.response.RevenueStatsResponse;
import com.example.taxi.entity.Ride;
import com.example.taxi.repository.RideRepository;
import com.example.taxi.service.StatsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatsServiceImpl implements StatsService {

    private final RideRepository rideRepository;

    public StatsServiceImpl(RideRepository rideRepository) {
        this.rideRepository = rideRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public RevenueStatsResponse revenue(LocalDate from, LocalDate to) {
        LocalDate start = from != null ? from : LocalDate.now().minusDays(14);
        LocalDate end = to != null ? to : LocalDate.now();
        if (end.isBefore(start)) {
            end = start;
        }
        List<Ride> completed = rideRepository.findCompletedInRange(
                start.atStartOfDay(), end.plusDays(1).atStartOfDay());

        BigDecimal totalRevenue = sum(completed.stream().map(Ride::getFare).toList());
        BigDecimal totalDriverRevenue = completed.stream()
                .map(r -> driverShare(r))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCommission = totalRevenue.subtract(totalDriverRevenue);
        BigDecimal averageRevenue = completed.isEmpty() ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(completed.size()), 2, RoundingMode.HALF_UP);

        return new RevenueStatsResponse(start, end, completed.size(),
                money(totalRevenue), money(totalCommission), money(totalDriverRevenue), money(averageRevenue),
                byDriver(completed), byVehicle(completed), byDayRevenue(completed));
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityStatsResponse activity(LocalDate from, LocalDate to) {
        LocalDate start = from != null ? from : LocalDate.now().minusDays(14);
        LocalDate end = to != null ? to : LocalDate.now();
        if (end.isBefore(start)) {
            end = start;
        }
        LocalDateTime fromTs = start.atStartOfDay();
        LocalDateTime toTs = end.plusDays(1).atStartOfDay();

        List<Ride> completed = rideRepository.findCompletedInRange(fromTs, toTs);
        long cancelled = rideRepository.countCanceledBetween(fromTs, toTs);

        long total = completed.size();
        BigDecimal totalRevenue = sum(completed.stream().map(Ride::getFare).toList());
        BigDecimal averageRevenue = total == 0 ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
        BigDecimal totalDistance = sum(completed.stream().map(Ride::getDistance).toList());
        BigDecimal rate = (total + cancelled) == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(cancelled * 100.0 / (total + cancelled)).setScale(2, RoundingMode.HALF_UP);

        List<Ride> sorted = completed.stream()
                .sorted(Comparator.comparing(Ride::getCompletedAt))
                .toList();
        Map<String, Long> byDriver = sorted.stream()
                .filter(r -> r.getDriver() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getDriver().fullName(),
                        LinkedHashMap::new,
                        Collectors.counting()));
        Map<String, Long> byVehicle = sorted.stream()
                .filter(r -> r.getVehicle() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getVehicle().getPlateNumber(),
                        LinkedHashMap::new,
                        Collectors.counting()));

        String mostActiveDriver = byDriver.entrySet().stream()
                .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("-");
        long mostActiveDriverRides = byDriver.values().stream().max(Long::compare).orElse(0L);
        String mostUsedVehicle = byVehicle.entrySet().stream()
                .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("-");
        long mostUsedVehicleRides = byVehicle.values().stream().max(Long::compare).orElse(0L);

        Map<LocalDate, List<Ride>> byDay = completed.stream()
                .collect(Collectors.groupingBy(r -> r.getCompletedAt().toLocalDate(), LinkedHashMap::new, Collectors.toList()));

        List<ActivityByDayResponse> days = byDay.entrySet().stream()
                .map(e -> new ActivityByDayResponse(e.getKey(), e.getValue().size(), 0L,
                        money(sum(e.getValue().stream().map(Ride::getFare).toList())),
                        money(sum(e.getValue().stream().map(Ride::getDistance).toList()))))
                .toList();

        return new ActivityStatsResponse(start, end, total, cancelled, money(rate), money(averageRevenue),
                money(totalDistance), mostActiveDriver, mostActiveDriverRides,
                mostUsedVehicle, mostUsedVehicleRides, days);
    }

    private List<RevenueBreakdownResponse> byDriver(List<Ride> completed) {
        return completed.stream()
                .filter(r -> r.getDriver() != null)
                .collect(Collectors.groupingBy(Ride::getDriver, LinkedHashMap::new, Collectors.toList()))
                .entrySet().stream()
                .map(e -> new RevenueBreakdownResponse(e.getKey().fullName(), e.getKey().getId(),
                        money(sum(e.getValue().stream().map(Ride::getFare).toList())), e.getValue().size()))
                .sorted(Comparator.comparing(RevenueBreakdownResponse::revenue).reversed())
                .toList();
    }

    private List<RevenueBreakdownResponse> byVehicle(List<Ride> completed) {
        return completed.stream()
                .filter(r -> r.getVehicle() != null)
                .collect(Collectors.groupingBy(Ride::getVehicle, LinkedHashMap::new, Collectors.toList()))
                .entrySet().stream()
                .map(e -> new RevenueBreakdownResponse(e.getKey().getPlateNumber(), e.getKey().getId(),
                        money(sum(e.getValue().stream().map(Ride::getFare).toList())), e.getValue().size()))
                .sorted(Comparator.comparing(RevenueBreakdownResponse::revenue).reversed())
                .toList();
    }

    private List<RevenueByDayResponse> byDayRevenue(List<Ride> completed) {
        Map<LocalDate, List<Ride>> byDay = completed.stream()
                .collect(Collectors.groupingBy(r -> r.getCompletedAt().toLocalDate(), LinkedHashMap::new, Collectors.toList()));
        return byDay.entrySet().stream()
                .map(e -> new RevenueByDayResponse(e.getKey(),
                        money(sum(e.getValue().stream().map(Ride::getFare).toList())),
                        money(sum(e.getValue().stream().map(this::driverShare).toList())),
                        e.getValue().size()))
                .sorted(Comparator.comparing(RevenueByDayResponse::date))
                .toList();
    }

    private BigDecimal driverShare(Ride ride) {
        if (ride.getDriver() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal rate = ride.getDriver().getCommissionRate() == null
                ? BigDecimal.ZERO : ride.getDriver().getCommissionRate();
        return ride.getFare().multiply(BigDecimal.valueOf(100)
                .subtract(rate).divide(BigDecimal.valueOf(100)));
    }

    private BigDecimal sum(List<BigDecimal> values) {
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}