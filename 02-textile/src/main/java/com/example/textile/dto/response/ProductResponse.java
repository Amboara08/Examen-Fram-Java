package com.example.textile.dto.response;

import java.time.*;
import java.math.*;
import java.util.*;
public record ProductResponse(Long id, Long version, String name, String reference, String description, BigDecimal unitPrice) {}
