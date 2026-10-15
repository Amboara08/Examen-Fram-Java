package com.example.tournois.dto.request;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.validation.constraints.*;
public record MatchRequest(
    @NotNull @Positive Long tournamentId,
    @Positive Long homeTeamId,
    @Positive Long awayTeamId,
    @NotNull LocalDateTime scheduledAt,
    @NotBlank @Size(max=2000) String location,
    @NotNull @Positive Integer roundNumber,
    @NotNull @PositiveOrZero Integer position
) {}
