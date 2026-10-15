package com.example.parking.dto.response;

import java.time.*;
import java.math.*;
import java.util.*;
public record ReservationResponse(Long id, Long version, Long customerId, Long vehicleId, Long spaceId, LocalDateTime arrival, LocalDateTime departure, String status) {}
