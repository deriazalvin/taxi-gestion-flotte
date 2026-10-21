package com.example.taxi.mapper;

import com.example.taxi.dto.request.DriverRequest;
import com.example.taxi.entity.Driver;

public final class DriverMapper {

    private DriverMapper() {
    }

    public static Driver toEntity(DriverRequest req) {
        Driver driver = new Driver(req.firstName(), req.lastName(), req.phone(),
                req.licenseNumber(), req.licenseExpiryDate(), req.hireDate());
        driver.setStatus(req.status());
        driver.setCommissionRate(req.commissionRate());
        return driver;
    }

    public static void update(Driver driver, DriverRequest req) {
        driver.setFirstName(req.firstName());
        driver.setLastName(req.lastName());
        driver.setPhone(req.phone());
        driver.setLicenseNumber(req.licenseNumber());
        driver.setLicenseExpiryDate(req.licenseExpiryDate());
        driver.setHireDate(req.hireDate());
        driver.setStatus(req.status());
        driver.setCommissionRate(req.commissionRate());
    }
}