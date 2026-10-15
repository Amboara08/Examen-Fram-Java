package com.example.textile.dto.request;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.validation.constraints.*;
public record QualityControlRequest(
    @NotNull @Positive Long orderId,
    @NotBlank @Size(max=2000) @Pattern(regexp="CONFORME|NON_CONFORME") String result,
    @NotBlank @Size(max=2000) String responsible,
    @Size(max=2000) String comment
) {}
