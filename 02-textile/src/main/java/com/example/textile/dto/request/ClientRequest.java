package com.example.textile.dto.request;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.validation.constraints.*;
public record ClientRequest(
    @NotBlank @Size(max=2000) String name,
    @NotBlank @Size(max=2000) @Email String email,
    @NotBlank @Size(max=2000) String phone,
    @NotBlank @Size(max=2000) String address
) {}
