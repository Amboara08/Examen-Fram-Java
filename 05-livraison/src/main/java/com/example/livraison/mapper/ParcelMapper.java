package com.example.livraison.mapper;

import com.example.livraison.entity.Parcel;
import com.example.livraison.dto.request.ParcelRequest;
import com.example.livraison.dto.response.ParcelResponse;
public final class ParcelMapper {
    private ParcelMapper() {}
    public static ParcelResponse response(Parcel e) { return new ParcelResponse(e.getId(), e.getVersion(), e.getOrderId(), e.getReference(), e.getDescription(), e.getWeight(), e.getVolume()); }
    public static void apply(ParcelRequest r, Parcel e) {
        e.setOrderId(r.orderId());
        e.setReference(r.reference());
        e.setDescription(r.description());
        e.setWeight(r.weight());
        e.setVolume(r.volume());
    }
}
