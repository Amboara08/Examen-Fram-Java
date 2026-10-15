package com.example.parking.entity;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.persistence.*;
@Entity
@Table(name="parkingspace_records")
public class ParkingSpace {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    @Column(unique=true, length=2000)
    private String number;
    @Column(length=2000)
    private String zone;
    @Column(length=2000)
    private String type = "STANDARD";
    @Column(length=2000)
    private String status = "LIBRE";

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public String getNumber() { return number; }
    public void setNumber(String value) { number = value; }
    public String getZone() { return zone; }
    public void setZone(String value) { zone = value; }
    public String getType() { return type; }
    public void setType(String value) { type = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
}
