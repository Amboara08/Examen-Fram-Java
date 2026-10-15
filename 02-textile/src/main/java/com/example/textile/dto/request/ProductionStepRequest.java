package com.example.textile.dto.request;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.validation.constraints.*;
public record ProductionStepRequest(
    @NotNull @Positive Long orderId,
    @NotBlank @Size(max=2000) @Pattern(regexp="COUPE|COUTURE|FINITION") String type,
    @NotBlank @Size(max=2000) String responsible,
    @NotNull LocalDateTime plannedStart,
    @NotNull LocalDateTime plannedEnd,
    @Size(max=2000) String comment
) {}
