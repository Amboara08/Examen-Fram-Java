package com.example.livraison.mapper;

import com.example.livraison.entity.Deliverer;
import com.example.livraison.dto.request.DelivererRequest;
import com.example.livraison.dto.response.DelivererResponse;
public final class DelivererMapper {
    private DelivererMapper() {}
    public static DelivererResponse response(Deliverer e) { return new DelivererResponse(e.getId(), e.getVersion(), e.getName(), e.getPhone(), e.getEmail(), e.getVehicle(), e.getZone(), e.getMaxWeight(), e.getMaxVolume(), e.getDistanceToPickup(), e.getMaxDeliveries(), e.getStatus()); }
    public static void apply(DelivererRequest r, Deliverer e) {
        e.setName(r.name());
        e.setPhone(r.phone());
        e.setEmail(r.email());
        e.setVehicle(r.vehicle());
        e.setZone("GENERAL");
        e.setMaxWeight(java.math.BigDecimal.valueOf(999999));
        e.setMaxVolume(java.math.BigDecimal.valueOf(999999));
        e.setDistanceToPickup(java.math.BigDecimal.ZERO);
        e.setMaxDeliveries(999999);
        e.setStatus("ACTIF");
    }
}
