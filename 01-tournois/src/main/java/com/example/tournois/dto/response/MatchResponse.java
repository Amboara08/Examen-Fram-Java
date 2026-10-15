package com.example.tournois.dto.response;

import java.time.*;
import java.math.*;
import java.util.*;
public record MatchResponse(Long id, Long version, Long tournamentId, Long homeTeamId, Long awayTeamId, LocalDateTime scheduledAt, String location, Integer roundNumber, Integer position, String status, Integer homeScore, Integer awayScore, Long winnerId) {}
