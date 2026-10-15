package com.example.taxis.dto.response;

import java.time.*;
import java.math.*;
import java.util.*;
public record DriverResponse(Long id, Long version, String name, String phone, String email, String licenseNumber, LocalDate licenseExpiry, LocalDate hiredAt, Long vehicleId, BigDecimal distanceToPickup, String status) {}
