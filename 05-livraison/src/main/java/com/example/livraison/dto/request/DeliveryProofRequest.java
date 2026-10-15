package com.example.livraison.dto.request;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.validation.constraints.*;
public record DeliveryProofRequest(
    @NotNull @Positive Long orderId,
    @NotBlank @Size(max=2000) String recipient,
    @NotBlank @Size(max=2000) String confirmationCode,
    @Size(max=2000) String comment,
    @NotNull LocalDateTime deliveredAt
) {}
