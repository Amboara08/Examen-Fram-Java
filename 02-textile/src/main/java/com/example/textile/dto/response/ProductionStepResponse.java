package com.example.textile.dto.response;

import java.time.*;
import java.math.*;
import java.util.*;
public record ProductionStepResponse(Long id, Long version, Long orderId, String type, String responsible, LocalDateTime plannedStart, LocalDateTime plannedEnd, String comment, String status, LocalDateTime startedAt, LocalDateTime completedAt, Integer quantity) {}
