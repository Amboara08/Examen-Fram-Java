package com.example.livraison.dto.request;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.validation.constraints.*;
public record IncidentRequest(
    @NotNull @Positive Long orderId,
    @NotBlank @Size(max=2000) @Pattern(regexp="CLIENT_ABSENT|ADRESSE_INCORRECTE|COLIS_ENDOMMAGE|PANNE|REFUS_CLIENT") String type,
    @NotBlank @Size(max=2000) String description,
    @Size(max=2000) String resolution
) {}
