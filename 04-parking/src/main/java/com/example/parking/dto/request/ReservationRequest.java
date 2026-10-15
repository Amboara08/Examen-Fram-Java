package com.example.parking.dto.request;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.validation.constraints.*;
public record ReservationRequest(
    @NotNull @Positive Long customerId,
    @Positive Long vehicleId,
    @Positive Long spaceId,
    @NotNull LocalDateTime arrival,
    @NotNull LocalDateTime departure
) {}
