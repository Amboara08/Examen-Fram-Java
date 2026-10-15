package com.example.parking.mapper;

import com.example.parking.entity.Subscription;
import com.example.parking.dto.request.SubscriptionRequest;
import com.example.parking.dto.response.SubscriptionResponse;
public final class SubscriptionMapper {
    private SubscriptionMapper() {}
    public static SubscriptionResponse response(Subscription e) { return new SubscriptionResponse(e.getId(), e.getVersion(), e.getCustomerId(), e.getVehicleId(), e.getType(), e.getStartDate(), e.getEndDate(), e.getStatus()); }
    public static void apply(SubscriptionRequest r, Subscription e) {
        e.setCustomerId(r.customerId());
        e.setVehicleId(r.vehicleId());
        e.setType(r.type());
        e.setStartDate(r.startDate());
        e.setEndDate(r.endDate());
        e.setStatus(r.status());
    }
}
