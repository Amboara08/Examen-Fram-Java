package com.example.tournois.mapper;

import com.example.tournois.entity.Team;
import com.example.tournois.dto.request.TeamRequest;
import com.example.tournois.dto.response.TeamResponse;
public final class TeamMapper {
    private TeamMapper() {}
    public static TeamResponse response(Team e) { return new TeamResponse(e.getId(), e.getVersion(), e.getName(), e.getLogo(), e.getCity(), e.getCoach(), e.getPlayers(), e.getStatus()); }
    public static void apply(TeamRequest r, Team e) {
        e.setName(r.name());
        e.setLogo(r.logo());
        e.setCity(r.city());
        e.setCoach(r.coach());
        e.setPlayers(r.players());
        e.setStatus(r.status());
    }
}
