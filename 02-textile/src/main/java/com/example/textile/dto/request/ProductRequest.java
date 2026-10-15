package com.example.textile.dto.request;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.validation.constraints.*;
public record ProductRequest(
    @NotBlank @Size(max=2000) String name,
    @NotBlank @Size(max=2000) String reference,
    @Size(max=2000) String description,
    @NotNull @Positive BigDecimal unitPrice
) {}
