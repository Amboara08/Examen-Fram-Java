package com.example.parking.mapper;

import com.example.parking.entity.Vehicle;
import com.example.parking.dto.request.VehicleRequest;
import com.example.parking.dto.response.VehicleResponse;
public final class VehicleMapper {
    private VehicleMapper() {}
    public static VehicleResponse response(Vehicle e) { return new VehicleResponse(e.getId(), e.getVersion(), e.getPlate(), e.getCustomerId(), e.getType(), e.getBrand(), e.getModel()); }
    public static void apply(VehicleRequest r, Vehicle e) {
        e.setPlate(r.plate());
        e.setCustomerId(r.customerId());
        e.setType(r.type());
        e.setBrand(r.brand());
        e.setModel(r.model());
    }
}
