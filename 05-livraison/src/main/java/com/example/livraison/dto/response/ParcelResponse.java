package com.example.livraison.dto.response;

import java.time.*;
import java.math.*;
import java.util.*;
public record ParcelResponse(Long id, Long version, Long orderId, String reference, String description, BigDecimal weight, BigDecimal volume) {}
