package com.example.parking.dto.request;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.validation.constraints.*;
public record VehicleRequest(
    @NotBlank @Size(max=2000) String plate,
    @NotNull @Positive Long customerId,
    @NotBlank @Size(max=2000) @Pattern(regexp="VOITURE|MOTO|UTILITAIRE") String type,
    @Size(max=120) String brand,
    @Size(max=120) String model
) {
    public VehicleRequest(String plate,Long customerId,String type) {
        this(plate,customerId,type,"","");
    }
}
