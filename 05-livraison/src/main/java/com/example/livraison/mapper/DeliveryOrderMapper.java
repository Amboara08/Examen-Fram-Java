package com.example.livraison.mapper;

import com.example.livraison.entity.DeliveryOrder;
import com.example.livraison.dto.request.DeliveryOrderRequest;
import com.example.livraison.dto.response.DeliveryOrderResponse;
public final class DeliveryOrderMapper {
    private DeliveryOrderMapper() {}
    public static DeliveryOrderResponse response(DeliveryOrder e) { return new DeliveryOrderResponse(e.getId(), e.getVersion(), e.getNumber(), e.getClientId(), e.getPickupAddress(), e.getDeliveryAddress(), e.getWeight(), e.getVolume(), e.getPriority(), e.getDistance(), e.getZone(), e.getExpectedAt(), e.getPrice(), e.getDelivererId(), e.getStatus(), e.getCreatedAt(), e.getDeliveredAt()); }
    public static void apply(DeliveryOrderRequest r, DeliveryOrder e) {
        e.setNumber(r.number());
        e.setClientId(r.clientId());
        e.setPickupAddress(r.pickupAddress());
        e.setDeliveryAddress(r.deliveryAddress());
        e.setWeight(r.weight());
        e.setVolume(r.volume());
        e.setPriority(r.priority());
        e.setDistance(r.distance());
        e.setZone(r.zone());
        e.setExpectedAt(r.expectedAt());
    }
}
