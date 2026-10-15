package com.example.textile.dto.request;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.validation.constraints.*;
public record TextileOrderRequest(
    @NotBlank @Size(max=2000) String number,
    @NotNull @Positive Long clientId,
    @NotNull LocalDate orderDate,
    @NotNull LocalDate deliveryDate,
    @NotBlank @Size(max=2000) @Pattern(regexp="NORMALE|URGENTE|TRES_URGENTE") String priority
) {}
