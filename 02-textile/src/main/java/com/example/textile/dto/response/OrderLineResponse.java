package com.example.textile.dto.response;

import java.time.*;
import java.math.*;
import java.util.*;
public record OrderLineResponse(Long id, Long version, Long orderId, Long productId, Integer quantity, String size, String color, BigDecimal unitPrice, Integer producedQuantity) {}
