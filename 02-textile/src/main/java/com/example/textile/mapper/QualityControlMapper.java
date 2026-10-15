package com.example.textile.mapper;

import com.example.textile.entity.QualityControl;
import com.example.textile.dto.request.QualityControlRequest;
import com.example.textile.dto.response.QualityControlResponse;
public final class QualityControlMapper {
    private QualityControlMapper() {}
    public static QualityControlResponse response(QualityControl e) { return new QualityControlResponse(e.getId(), e.getVersion(), e.getOrderId(), e.getResult(), e.getResponsible(), e.getComment(), e.getCheckedAt()); }
    public static void apply(QualityControlRequest r, QualityControl e) {
        e.setOrderId(r.orderId());
        e.setResult(r.result());
        e.setResponsible(r.responsible());
        e.setComment(r.comment());
    }
}
