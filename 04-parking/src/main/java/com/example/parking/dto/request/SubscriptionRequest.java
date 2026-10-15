package com.example.parking.dto.request;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.validation.constraints.*;
public record SubscriptionRequest(
    @NotNull @Positive Long customerId,
    @NotNull @Positive Long vehicleId,
    @NotBlank @Size(max=2000) @Pattern(regexp="JOURNALIER|HEBDOMADAIRE|MENSUEL|ANNUEL") String type,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    @NotBlank @Size(max=2000) @Pattern(regexp="ACTIF|ANNULE") String status
) {}
