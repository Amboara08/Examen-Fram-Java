package com.example.textile.mapper;

import com.example.textile.entity.ProductionStep;
import com.example.textile.dto.request.ProductionStepRequest;
import com.example.textile.dto.response.ProductionStepResponse;
public final class ProductionStepMapper {
    private ProductionStepMapper() {}
    public static ProductionStepResponse response(ProductionStep e) { return new ProductionStepResponse(e.getId(), e.getVersion(), e.getOrderId(), e.getType(), e.getResponsible(), e.getPlannedStart(), e.getPlannedEnd(), e.getComment(), e.getStatus(), e.getStartedAt(), e.getCompletedAt(), e.getQuantity()); }
    public static void apply(ProductionStepRequest r, ProductionStep e) {
        e.setOrderId(r.orderId());
        e.setType(r.type());
        e.setResponsible(r.responsible());
        e.setPlannedStart(r.plannedStart());
        e.setPlannedEnd(r.plannedEnd());
        e.setComment(r.comment());
    }
}
