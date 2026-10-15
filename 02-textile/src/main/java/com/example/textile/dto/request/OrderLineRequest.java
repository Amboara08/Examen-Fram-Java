package com.example.textile.dto.request;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.validation.constraints.*;
public record OrderLineRequest(
    @NotNull @Positive Long orderId,
    @NotNull @Positive Long productId,
    @NotNull @Positive Integer quantity,
    @NotBlank @Size(max=2000) String size,
    @NotBlank @Size(max=2000) String color,
    @NotNull @Positive BigDecimal unitPrice
) {}
