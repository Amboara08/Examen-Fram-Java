package com.example.tournois.dto.request;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.validation.constraints.*;
public record RegistrationRequest(
    @NotNull @Positive Long tournamentId,
    @NotNull @Positive Long teamId
) {}
