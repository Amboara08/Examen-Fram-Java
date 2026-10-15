package com.example.livraison.dto.request;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.validation.constraints.*;
public record DelivererRequest(
    @NotBlank @Size(max=2000) String name,
    @NotBlank @Size(max=2000) String phone,
    @NotBlank @Email @Size(max=2000) String email,
    @NotBlank @Size(max=2000) String vehicle
) {
    public DelivererRequest(String name,String phone,String vehicle,String zone,BigDecimal maxWeight,BigDecimal maxVolume,BigDecimal distanceToPickup,Integer maxDeliveries,String status){this(name,phone,name.toLowerCase().replace(" ",".")+"@livreur.local",vehicle);}
}
