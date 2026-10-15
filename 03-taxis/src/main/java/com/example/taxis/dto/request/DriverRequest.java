package com.example.taxis.dto.request;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.validation.constraints.*;
public record DriverRequest(
    @NotBlank @Size(max=2000) String name,
    @NotBlank @Size(max=2000) String phone,
    @NotBlank @Email @Size(max=2000) String email,
    @NotBlank @Size(max=2000) String licenseNumber,
    @NotNull LocalDate licenseExpiry,
    @NotNull LocalDate hiredAt,
    @Positive Long vehicleId,
    @NotNull @PositiveOrZero BigDecimal distanceToPickup,
    @NotBlank @Size(max=2000) @Pattern(regexp="DISPONIBLE|EN_PAUSE|INDISPONIBLE|SUSPENDU") String status
) { public DriverRequest(String name,String phone,String licenseNumber,LocalDate licenseExpiry,LocalDate hiredAt,Long vehicleId,BigDecimal distanceToPickup,String status){this(name,phone,name.toLowerCase().replace(" ",".")+"@chauffeur.local",licenseNumber,licenseExpiry,hiredAt,vehicleId,distanceToPickup,status);} }
