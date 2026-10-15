package com.example.taxis.dto.request;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.validation.constraints.*;
public record VehicleRequest(
    @NotBlank @Size(max=2000) String plate,
    @NotBlank @Size(max=2000) String brand,
    @NotBlank @Size(max=2000) String model,
    @NotNull @Positive Integer year,
    @NotNull @Positive Integer capacity,
    @NotNull @PositiveOrZero BigDecimal mileage,
    @NotBlank @Size(max=2000) @Pattern(regexp="DISPONIBLE|MAINTENANCE|HORS_SERVICE") String status
) {}
