package com.example.livraison.dto.request;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.validation.constraints.*;
public record ParcelRequest(
    @NotNull @Positive Long orderId,
    @NotBlank @Size(max=2000) String reference,
    @NotBlank @Size(max=2000) String description,
    @NotNull @Positive BigDecimal weight,
    @NotNull @Positive BigDecimal volume
) {}
