package com.example.textile.dto.response;

import java.time.*;
import java.math.*;
import java.util.*;
public record TextileOrderResponse(Long id, Long version, String number, Long clientId, LocalDate orderDate, LocalDate deliveryDate, String priority, String status, LocalDateTime deliveredAt) {}
