package com.example.tournois.entity;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.persistence.*;
@Entity
@Table(name="team_records")
public class Team {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    @Column(unique=true, length=2000)
    private String name;
    @Column(length=2000)
    private String logo;
    @Column(length=2000)
    private String city;
    @Column(length=2000)
    private String coach;
    @Column(length=2000)
    private String players;
    @Column(length=2000)
    private String status = "ACTIVE";

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public String getName() { return name; }
    public void setName(String value) { name = value; }
    public String getLogo() { return logo; }
    public void setLogo(String value) { logo = value; }
    public String getCity() { return city; }
    public void setCity(String value) { city = value; }
    public String getCoach() { return coach; }
    public void setCoach(String value) { coach = value; }
    public String getPlayers() { return players; }
    public void setPlayers(String value) { players = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
}
