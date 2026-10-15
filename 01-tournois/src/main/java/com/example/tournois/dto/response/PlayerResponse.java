package com.example.tournois.dto.response;

public record PlayerResponse(
    Long id, Long teamId, Integer number, String name, Integer heightCm, String position
) {}
