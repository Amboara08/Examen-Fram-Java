package com.example.tournois.dto.request;

import jakarta.validation.constraints.*;

public record PlayerRequest(
    @NotNull @Min(1) @Max(99) Integer number,
    @NotBlank @Size(max = 120) String name,
    @NotNull @Min(100) @Max(250) Integer heightCm,
    @NotBlank @Pattern(regexp = "GARDIEN|DEFENSEUR|MILIEU|ATTAQUANT") String position
) {}
