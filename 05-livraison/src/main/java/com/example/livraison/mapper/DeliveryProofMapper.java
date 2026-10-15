package com.example.livraison.mapper;

import com.example.livraison.entity.DeliveryProof;
import com.example.livraison.dto.request.DeliveryProofRequest;
import com.example.livraison.dto.response.DeliveryProofResponse;
public final class DeliveryProofMapper {
    private DeliveryProofMapper() {}
    public static DeliveryProofResponse response(DeliveryProof e) { return new DeliveryProofResponse(e.getId(), e.getVersion(), e.getOrderId(), e.getRecipient(), e.getConfirmationCode(), e.getComment(), e.getDeliveredAt()); }
    public static void apply(DeliveryProofRequest r, DeliveryProof e) {
        e.setOrderId(r.orderId());
        e.setRecipient(r.recipient());
        e.setConfirmationCode(r.confirmationCode());
        e.setComment(r.comment());
        e.setDeliveredAt(r.deliveredAt());
    }
}
