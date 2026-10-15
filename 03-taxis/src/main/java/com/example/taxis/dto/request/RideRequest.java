package com.example.taxis.dto.request;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.validation.constraints.*;
public record RideRequest(
    @NotNull @Positive Long clientId,
    @NotBlank @Size(max=2000) String departure,
    @NotBlank @Size(max=2000) String destination,
    @NotNull @Positive BigDecimal distance,
    @NotNull LocalDateTime scheduledAt,
    @NotNull @Positive Integer passengers
) {}
