package com.example.taxis.mapper;

import com.example.taxis.entity.Driver;
import com.example.taxis.dto.request.DriverRequest;
import com.example.taxis.dto.response.DriverResponse;
public final class DriverMapper {
    private DriverMapper() {}
    public static DriverResponse response(Driver e) { return new DriverResponse(e.getId(), e.getVersion(), e.getName(), e.getPhone(), e.getEmail(), e.getLicenseNumber(), e.getLicenseExpiry(), e.getHiredAt(), e.getVehicleId(), e.getDistanceToPickup(), e.getStatus()); }
    public static void apply(DriverRequest r, Driver e) {
        e.setName(r.name());
        e.setPhone(r.phone());
        e.setEmail(r.email());
        e.setLicenseNumber(r.licenseNumber());
        e.setLicenseExpiry(r.licenseExpiry());
        e.setHiredAt(r.hiredAt());
        e.setVehicleId(r.vehicleId());
        e.setDistanceToPickup(r.distanceToPickup());
        e.setStatus(r.status());
    }
}
