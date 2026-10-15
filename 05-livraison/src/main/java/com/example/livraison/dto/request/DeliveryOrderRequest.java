package com.example.livraison.dto.request;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.validation.constraints.*;
public record DeliveryOrderRequest(
    @NotBlank @Size(max=2000) String number,
    @NotNull @Positive Long clientId,
    @NotBlank @Size(max=2000) String pickupAddress,
    @NotBlank @Size(max=2000) String deliveryAddress,
    @NotNull @Positive BigDecimal weight,
    @NotNull @Positive BigDecimal volume,
    @NotBlank @Size(max=2000) @Pattern(regexp="NORMALE|URGENTE|TRES_URGENTE") String priority,
    @NotNull @Positive BigDecimal distance,
    @NotBlank @Size(max=2000) String zone,
    @NotNull LocalDateTime expectedAt
) {}
