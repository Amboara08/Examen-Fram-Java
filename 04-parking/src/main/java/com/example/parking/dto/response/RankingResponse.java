package com.example.parking.dto.response;

public record RankingResponse(Long teamId,String name,int played,int wins,int draws,int losses,int points,int goalsFor,int goalsAgainst,int difference) {}
