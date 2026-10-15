package com.example.tournois.mapper;

import com.example.tournois.entity.Tournament;
import com.example.tournois.dto.request.TournamentRequest;
import com.example.tournois.dto.response.TournamentResponse;
public final class TournamentMapper {
    private TournamentMapper() {}
    public static TournamentResponse response(Tournament e) { return new TournamentResponse(e.getId(), e.getVersion(), e.getName(), e.getSport(), e.getDescription(), e.getStartDate(), e.getEndDate(), e.getLocation(), e.getMaxTeams(), e.getFormat(), e.getRankingCriteria(), e.getStatus()); }
    public static void apply(TournamentRequest r, Tournament e) {
        e.setName(r.name());
        e.setSport(r.sport());
        e.setDescription(r.description());
        e.setStartDate(r.startDate());
        e.setEndDate(r.endDate());
        e.setLocation(r.location());
        e.setMaxTeams(r.maxTeams());
        e.setFormat(r.format());
        e.setRankingCriteria(r.rankingCriteria());
    }
}
