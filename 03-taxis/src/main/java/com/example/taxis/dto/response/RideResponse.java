package com.example.taxis.dto.response;

import java.time.*;
import java.math.*;
import java.util.*;
public record RideResponse(Long id, Long version, Long clientId, String departure, String destination, BigDecimal distance, LocalDateTime scheduledAt, Integer passengers, BigDecimal fare, Long driverId, Long vehicleId, String status, LocalDateTime startedAt, LocalDateTime completedAt) {}
