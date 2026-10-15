package com.example.textile.mapper;

import com.example.textile.entity.TextileOrder;
import com.example.textile.dto.request.TextileOrderRequest;
import com.example.textile.dto.response.TextileOrderResponse;
public final class TextileOrderMapper {
    private TextileOrderMapper() {}
    public static TextileOrderResponse response(TextileOrder e) { return new TextileOrderResponse(e.getId(), e.getVersion(), e.getNumber(), e.getClientId(), e.getOrderDate(), e.getDeliveryDate(), e.getPriority(), e.getStatus(), e.getDeliveredAt()); }
    public static void apply(TextileOrderRequest r, TextileOrder e) {
        e.setNumber(r.number());
        e.setClientId(r.clientId());
        e.setOrderDate(r.orderDate());
        e.setDeliveryDate(r.deliveryDate());
        e.setPriority(r.priority());
    }
}
