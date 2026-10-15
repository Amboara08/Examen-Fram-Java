package com.example.livraison.dto.response;

import java.time.*;
import java.math.*;
import java.util.*;
public record DelivererResponse(Long id, Long version, String name, String phone, String email, String vehicle, String zone, BigDecimal maxWeight, BigDecimal maxVolume, BigDecimal distanceToPickup, Integer maxDeliveries, String status) {}
