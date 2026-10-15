package com.example.textile.dto.response;

import java.time.*;
import java.math.*;
import java.util.*;
public record ClientResponse(Long id, Long version, String name, String email, String phone, String address) {}
