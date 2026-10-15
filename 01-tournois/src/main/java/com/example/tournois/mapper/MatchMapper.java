package com.example.tournois.mapper;

import com.example.tournois.entity.Match;
import com.example.tournois.dto.request.MatchRequest;
import com.example.tournois.dto.response.MatchResponse;
public final class MatchMapper {
    private MatchMapper() {}
    public static MatchResponse response(Match e) { return new MatchResponse(e.getId(), e.getVersion(), e.getTournamentId(), e.getHomeTeamId(), e.getAwayTeamId(), e.getScheduledAt(), e.getLocation(), e.getRoundNumber(), e.getPosition(), e.getStatus(), e.getHomeScore(), e.getAwayScore(), e.getWinnerId()); }
    public static void apply(MatchRequest r, Match e) {
        e.setTournamentId(r.tournamentId());
        e.setHomeTeamId(r.homeTeamId());
        e.setAwayTeamId(r.awayTeamId());
        e.setScheduledAt(r.scheduledAt());
        e.setLocation(r.location());
        e.setRoundNumber(r.roundNumber());
        e.setPosition(r.position());
    }
}
