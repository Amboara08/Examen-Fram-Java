package com.example.parking.dto.response;

import java.time.*;
import java.math.*;
import java.util.*;
public record SubscriptionResponse(Long id, Long version, Long customerId, Long vehicleId, String type, LocalDate startDate, LocalDate endDate, String status) {}
