package com.example.livraison.dto.response;

import java.time.*;
import java.math.*;
import java.util.*;
public record IncidentResponse(Long id, Long version, Long orderId, String type, String description, String resolution, Long delivererId, LocalDateTime occurredAt) {}
