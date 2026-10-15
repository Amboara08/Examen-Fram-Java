package com.example.livraison.dto.request;

import jakarta.validation.constraints.*;
public record SettingRequest(@NotBlank @Pattern(regexp="[a-zA-Z][a-zA-Z0-9.]{0,80}") String name,@DecimalMin("0.0") @DecimalMax("1000000") double value) {}
