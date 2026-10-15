package com.example.tournois.dto.request;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.validation.constraints.*;
public record TeamRequest(
    @NotBlank @Size(max=2000) String name,
    @Size(max=2000) String logo,
    @NotBlank @Size(max=2000) String city,
    @NotBlank @Size(max=2000) String coach,
    @Size(max=2000) String players,
    @NotBlank @Size(max=2000) @Pattern(regexp="ACTIVE|INACTIVE") String status
) {}
