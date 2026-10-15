package com.example.livraison.mapper;

import com.example.livraison.entity.Incident;
import com.example.livraison.dto.request.IncidentRequest;
import com.example.livraison.dto.response.IncidentResponse;
public final class IncidentMapper {
    private IncidentMapper() {}
    public static IncidentResponse response(Incident e) { return new IncidentResponse(e.getId(), e.getVersion(), e.getOrderId(), e.getType(), e.getDescription(), e.getResolution(), e.getDelivererId(), e.getOccurredAt()); }
    public static void apply(IncidentRequest r, Incident e) {
        e.setOrderId(r.orderId());
        e.setType(r.type());
        e.setDescription(r.description());
        e.setResolution(r.resolution());
    }
}
