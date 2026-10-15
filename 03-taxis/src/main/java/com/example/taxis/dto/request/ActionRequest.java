package com.example.taxis.dto.request;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.validation.constraints.*;
public record ActionRequest(
 @Size(max=50) String status, @Positive Long teamId, @PositiveOrZero Integer quantity,
 @PositiveOrZero Integer homeScore, @PositiveOrZero Integer awayScore,
 @Size(max=200) String responsible, @Size(max=2000) String comment,
 @Size(max=200) String recipient, @Size(max=100) String confirmationCode,
 LocalDateTime at, LocalDateTime plannedEnd, @Positive Long stayId
) { public static ActionRequest empty(){return new ActionRequest(null,null,null,null,null,null,null,null,null,null,null,null);} }
