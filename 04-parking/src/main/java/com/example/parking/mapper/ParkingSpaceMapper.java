package com.example.parking.mapper;

import com.example.parking.entity.ParkingSpace;
import com.example.parking.dto.request.ParkingSpaceRequest;
import com.example.parking.dto.response.ParkingSpaceResponse;
public final class ParkingSpaceMapper {
    private ParkingSpaceMapper() {}
    public static ParkingSpaceResponse response(ParkingSpace e) { return new ParkingSpaceResponse(e.getId(), e.getVersion(), e.getNumber(), e.getZone(), e.getType(), e.getStatus()); }
    public static void apply(ParkingSpaceRequest r, ParkingSpace e) {
        e.setNumber(r.number());
        e.setZone(r.zone());
        e.setType(r.type());
        e.setStatus(r.status());
    }
}
