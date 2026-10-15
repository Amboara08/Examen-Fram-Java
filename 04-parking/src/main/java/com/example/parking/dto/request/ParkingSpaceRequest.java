package com.example.parking.dto.request;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.validation.constraints.*;
public record ParkingSpaceRequest(
    @NotBlank @Size(max=2000) String number,
    @NotBlank @Size(max=2000) String zone,
    @NotBlank @Size(max=2000) @Pattern(regexp="STANDARD|PMR|VIP|ABONNE") String type,
    @NotBlank @Size(max=2000) @Pattern(regexp="LIBRE|HORS_SERVICE") String status
) {}
