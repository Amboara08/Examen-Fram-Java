package com.example.taxis.mapper;

import com.example.taxis.entity.Vehicle;
import com.example.taxis.dto.request.VehicleRequest;
import com.example.taxis.dto.response.VehicleResponse;
public final class VehicleMapper {
    private VehicleMapper() {}
    public static VehicleResponse response(Vehicle e) { return new VehicleResponse(e.getId(), e.getVersion(), e.getPlate(), e.getBrand(), e.getModel(), e.getYear(), e.getCapacity(), e.getMileage(), e.getStatus()); }
    public static void apply(VehicleRequest r, Vehicle e) {
        e.setPlate(r.plate());
        e.setBrand(r.brand());
        e.setModel(r.model());
        e.setYear(r.year());
        e.setCapacity(r.capacity());
        e.setMileage(r.mileage());
        e.setStatus(r.status());
    }
}
