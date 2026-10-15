package com.example.textile.mapper;

import com.example.textile.entity.OrderLine;
import com.example.textile.dto.request.OrderLineRequest;
import com.example.textile.dto.response.OrderLineResponse;
public final class OrderLineMapper {
    private OrderLineMapper() {}
    public static OrderLineResponse response(OrderLine e) { return new OrderLineResponse(e.getId(), e.getVersion(), e.getOrderId(), e.getProductId(), e.getQuantity(), e.getSize(), e.getColor(), e.getUnitPrice(), e.getProducedQuantity()); }
    public static void apply(OrderLineRequest r, OrderLine e) {
        e.setOrderId(r.orderId());
        e.setProductId(r.productId());
        e.setQuantity(r.quantity());
        e.setSize(r.size());
        e.setColor(r.color());
        e.setUnitPrice(r.unitPrice());
    }
}
