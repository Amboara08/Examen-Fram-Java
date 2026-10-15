package com.example.livraison.dto.response;

import java.time.*;
import java.math.*;
import java.util.*;
public record DeliveryOrderResponse(Long id, Long version, String number, Long clientId, String pickupAddress, String deliveryAddress, BigDecimal weight, BigDecimal volume, String priority, BigDecimal distance, String zone, LocalDateTime expectedAt, BigDecimal price, Long delivererId, String status, LocalDateTime createdAt, LocalDateTime deliveredAt) {}
