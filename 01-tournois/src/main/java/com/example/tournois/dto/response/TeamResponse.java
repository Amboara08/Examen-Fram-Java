package com.example.tournois.dto.response;

import java.time.*;
import java.math.*;
import java.util.*;
public record TeamResponse(Long id, Long version, String name, String logo, String city, String coach, String players, String status) {}
