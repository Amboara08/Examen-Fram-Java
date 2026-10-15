package com.example.tournois.dto.response;

import java.time.*;
import java.math.*;
import java.util.*;
public record RegistrationResponse(Long id, Long version, Long tournamentId, Long teamId) {}
