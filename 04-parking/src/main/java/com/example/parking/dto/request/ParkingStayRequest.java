package com.example.parking.dto.request;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.validation.constraints.*;
public record ParkingStayRequest(
    @NotNull @Positive Long customerId,
    @Positive Long vehicleId,
    @Positive Long spaceId,
    @Positive Long reservationId,
    @NotNull LocalDateTime enteredAt,
    @NotNull LocalDateTime expectedExit
) {}
