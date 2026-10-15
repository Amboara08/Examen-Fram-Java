package com.example.taxis.mapper;

import com.example.taxis.entity.Ride;
import com.example.taxis.dto.request.RideRequest;
import com.example.taxis.dto.response.RideResponse;
public final class RideMapper {
    private RideMapper() {}
    public static RideResponse response(Ride e) { return new RideResponse(e.getId(), e.getVersion(), e.getClientId(), e.getDeparture(), e.getDestination(), e.getDistance(), e.getScheduledAt(), e.getPassengers(), e.getFare(), e.getDriverId(), e.getVehicleId(), e.getStatus(), e.getStartedAt(), e.getCompletedAt()); }
    public static void apply(RideRequest r, Ride e) {
        e.setClientId(r.clientId());
        e.setDeparture(r.departure());
        e.setDestination(r.destination());
        e.setDistance(r.distance());
        e.setScheduledAt(r.scheduledAt());
        e.setPassengers(r.passengers());
    }
}
