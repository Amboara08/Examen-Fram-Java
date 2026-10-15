package com.example.parking.mapper;

import com.example.parking.entity.Reservation;
import com.example.parking.dto.request.ReservationRequest;
import com.example.parking.dto.response.ReservationResponse;
public final class ReservationMapper {
    private ReservationMapper() {}
    public static ReservationResponse response(Reservation e) { return new ReservationResponse(e.getId(), e.getVersion(), e.getCustomerId(), e.getVehicleId(), e.getSpaceId(), e.getArrival(), e.getDeparture(), e.getStatus()); }
    public static void apply(ReservationRequest r, Reservation e) {
        e.setCustomerId(r.customerId());
        e.setVehicleId(r.vehicleId());
        e.setSpaceId(r.spaceId());
        e.setArrival(r.arrival());
        e.setDeparture(r.departure());
    }
}
