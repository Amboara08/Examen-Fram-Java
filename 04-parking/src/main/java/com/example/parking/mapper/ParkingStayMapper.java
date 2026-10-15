package com.example.parking.mapper;

import com.example.parking.entity.ParkingStay;
import com.example.parking.dto.request.ParkingStayRequest;
import com.example.parking.dto.response.ParkingStayResponse;
public final class ParkingStayMapper {
    private ParkingStayMapper() {}
    public static ParkingStayResponse response(ParkingStay e) { return new ParkingStayResponse(e.getId(), e.getVersion(), e.getCustomerId(), e.getVehicleId(), e.getSpaceId(), e.getReservationId(), e.getEnteredAt(), e.getExpectedExit(), e.getExitedAt(), e.getAmount(), e.getStatus()); }
    public static void apply(ParkingStayRequest r, ParkingStay e) {
        e.setCustomerId(r.customerId());
        e.setVehicleId(r.vehicleId());
        e.setSpaceId(r.spaceId());
        e.setReservationId(r.reservationId());
        e.setEnteredAt(r.enteredAt());
        e.setExpectedExit(r.expectedExit());
    }
}
