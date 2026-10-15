package com.example.tournois.entity;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.persistence.*;
@Entity
@Table(name="tournament_records")
public class Tournament {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    @Column(length=2000)
    private String name;
    @Column(length=2000)
    private String sport;
    @Column(length=2000)
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    @Column(length=2000)
    private String location;
    private Integer maxTeams;
    @Column(length=2000)
    private String format = "CHAMPIONNAT";
    @Column(length=2000)
    private String rankingCriteria = "POINTS,DIFFERENCE,BUTS,CONFRONTATION";
    @Column(length=2000)
    private String status = "BROUILLON";

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public String getName() { return name; }
    public void setName(String value) { name = value; }
    public String getSport() { return sport; }
    public void setSport(String value) { sport = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { description = value; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate value) { startDate = value; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate value) { endDate = value; }
    public String getLocation() { return location; }
    public void setLocation(String value) { location = value; }
    public Integer getMaxTeams() { return maxTeams; }
    public void setMaxTeams(Integer value) { maxTeams = value; }
    public String getFormat() { return format; }
    public void setFormat(String value) { format = value; }
    public String getRankingCriteria() { return rankingCriteria; }
    public void setRankingCriteria(String value) { rankingCriteria = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
}
