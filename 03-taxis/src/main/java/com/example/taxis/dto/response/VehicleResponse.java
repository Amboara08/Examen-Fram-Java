package com.example.taxis.dto.response;

import java.time.*;
import java.math.*;
import java.util.*;
public record VehicleResponse(Long id, Long version, String plate, String brand, String model, Integer year, Integer capacity, BigDecimal mileage, String status) {}
