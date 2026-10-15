package com.example.tournois.entity;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.persistence.*;
@Entity
@Table(name="match_records")
public class Match {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    private Long tournamentId;
    private Long homeTeamId;
    private Long awayTeamId;
    private LocalDateTime scheduledAt;
    @Column(length=2000)
    private String location;
    private Integer roundNumber;
    private Integer position;
    @Column(length=2000)
    private String status = "PROGRAMME";
    private Integer homeScore;
    private Integer awayScore;
    private Long winnerId;

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public Long getTournamentId() { return tournamentId; }
    public void setTournamentId(Long value) { tournamentId = value; }
    public Long getHomeTeamId() { return homeTeamId; }
    public void setHomeTeamId(Long value) { homeTeamId = value; }
    public Long getAwayTeamId() { return awayTeamId; }
    public void setAwayTeamId(Long value) { awayTeamId = value; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime value) { scheduledAt = value; }
    public String getLocation() { return location; }
    public void setLocation(String value) { location = value; }
    public Integer getRoundNumber() { return roundNumber; }
    public void setRoundNumber(Integer value) { roundNumber = value; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer value) { position = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public Integer getHomeScore() { return homeScore; }
    public void setHomeScore(Integer value) { homeScore = value; }
    public Integer getAwayScore() { return awayScore; }
    public void setAwayScore(Integer value) { awayScore = value; }
    public Long getWinnerId() { return winnerId; }
    public void setWinnerId(Long value) { winnerId = value; }
}
