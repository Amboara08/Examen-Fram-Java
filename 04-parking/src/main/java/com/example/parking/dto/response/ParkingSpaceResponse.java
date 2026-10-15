package com.example.parking.dto.response;

import java.time.*;
import java.math.*;
import java.util.*;
public record ParkingSpaceResponse(Long id, Long version, String number, String zone, String type, String status) {}
