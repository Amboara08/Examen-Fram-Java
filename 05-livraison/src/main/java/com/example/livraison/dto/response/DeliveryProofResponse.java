package com.example.livraison.dto.response;

import java.time.*;
import java.math.*;
import java.util.*;
public record DeliveryProofResponse(Long id, Long version, Long orderId, String recipient, String confirmationCode, String comment, LocalDateTime deliveredAt) {}
