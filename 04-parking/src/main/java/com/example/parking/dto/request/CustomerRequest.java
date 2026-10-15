package com.example.parking.dto.request;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.validation.constraints.*;
public record CustomerRequest(
    @NotBlank @Size(max=2000) String name,
    @NotBlank @Size(max=2000) @Email String email,
    @NotBlank @Size(max=2000) String phone,
    @NotBlank @Size(max=2000) @Pattern(regexp="STANDARD|PMR|VIP|ABONNE") String type
) {}
