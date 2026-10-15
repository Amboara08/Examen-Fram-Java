package com.example.tournois.dto.request;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.validation.constraints.*;
public record TournamentRequest(
    @NotBlank @Size(max=2000) String name,
    @NotBlank @Size(max=2000) String sport,
    @Size(max=2000) String description,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    @NotBlank @Size(max=2000) String location,
    @NotNull @Positive Integer maxTeams,
    @NotBlank @Size(max=2000) @Pattern(regexp="CHAMPIONNAT|ALLER_RETOUR|ELIMINATION") String format,
    @NotBlank @Size(max=2000) String rankingCriteria
) {}
