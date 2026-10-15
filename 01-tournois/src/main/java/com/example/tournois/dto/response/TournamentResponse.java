package com.example.tournois.dto.response;

import java.time.*;
import java.math.*;
import java.util.*;
public record TournamentResponse(Long id, Long version, String name, String sport, String description, LocalDate startDate, LocalDate endDate, String location, Integer maxTeams, String format, String rankingCriteria, String status) {}
