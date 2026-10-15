package com.example.tournois.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "player_records", uniqueConstraints =
    @UniqueConstraint(name = "uk_player_team_number", columnNames = {"team_id", "jersey_number"}))
public class Player {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version
    private Long version;
    @Column(name = "team_id", nullable = false)
    private Long teamId;
    @Column(name = "jersey_number", nullable = false)
    private Integer number;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(name = "height_cm", nullable = false)
    private Integer heightCm;
    @Column(nullable = false, length = 40)
    private String position;

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public Integer getNumber() { return number; }
    public void setNumber(Integer number) { this.number = number; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getHeightCm() { return heightCm; }
    public void setHeightCm(Integer heightCm) { this.heightCm = heightCm; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
}
