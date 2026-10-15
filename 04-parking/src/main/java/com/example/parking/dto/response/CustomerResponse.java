package com.example.parking.dto.response;

import java.time.*;
import java.math.*;
import java.util.*;
public record CustomerResponse(Long id, Long version, String name, String email, String phone, String type) {}
