package com.example.tournois.mapper;

import com.example.tournois.entity.Registration;
import com.example.tournois.dto.request.RegistrationRequest;
import com.example.tournois.dto.response.RegistrationResponse;
public final class RegistrationMapper {
    private RegistrationMapper() {}
    public static RegistrationResponse response(Registration e) { return new RegistrationResponse(e.getId(), e.getVersion(), e.getTournamentId(), e.getTeamId()); }
    public static void apply(RegistrationRequest r, Registration e) {
        e.setTournamentId(r.tournamentId());
        e.setTeamId(r.teamId());
    }
}
