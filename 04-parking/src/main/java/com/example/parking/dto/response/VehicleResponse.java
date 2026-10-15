package com.example.parking.dto.response;

import java.time.*;
import java.math.*;
import java.util.*;
public record VehicleResponse(Long id, Long version, String plate, Long customerId, String type, String brand, String model) {}
