package com.example.tournois.entity;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.persistence.*;
@Entity
@Table(name="registration_records")
public class Registration {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    private Long tournamentId;
    private Long teamId;

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public Long getTournamentId() { return tournamentId; }
    public void setTournamentId(Long value) { tournamentId = value; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long value) { teamId = value; }
}
