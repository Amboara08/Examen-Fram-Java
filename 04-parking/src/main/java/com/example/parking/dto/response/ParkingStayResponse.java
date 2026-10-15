package com.example.parking.dto.response;

import java.time.*;
import java.math.*;
import java.util.*;
public record ParkingStayResponse(Long id, Long version, Long customerId, Long vehicleId, Long spaceId, Long reservationId, LocalDateTime enteredAt, LocalDateTime expectedExit, LocalDateTime exitedAt, BigDecimal amount, String status) {}
