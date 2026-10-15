package com.example.livraison.entity;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.persistence.*;
@Entity
@Table(name="deliverer_records")
public class Deliverer {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    @Column(length=2000)
    private String name;
    @Column(length=2000)
    private String phone;
    @Column(unique=true,length=2000)
    private String email;
    @Column(name="password_hash",length=100)
    private String passwordHash;
    @Column(length=2000)
    private String vehicle;
    @Column(length=2000)
    private String zone;
    private BigDecimal maxWeight;
    private BigDecimal maxVolume;
    private BigDecimal distanceToPickup;
    private Integer maxDeliveries;
    @Column(length=2000)
    private String status = "DISPONIBLE";

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public String getName() { return name; }
    public void setName(String value) { name = value; }
    public String getPhone() { return phone; }
    public void setPhone(String value) { phone = value; }
    public String getEmail() { return email; }
    public void setEmail(String value) { email = value; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String value) { passwordHash = value; }
    public String getVehicle() { return vehicle; }
    public void setVehicle(String value) { vehicle = value; }
    public String getZone() { return zone; }
    public void setZone(String value) { zone = value; }
    public BigDecimal getMaxWeight() { return maxWeight; }
    public void setMaxWeight(BigDecimal value) { maxWeight = value; }
    public BigDecimal getMaxVolume() { return maxVolume; }
    public void setMaxVolume(BigDecimal value) { maxVolume = value; }
    public BigDecimal getDistanceToPickup() { return distanceToPickup; }
    public void setDistanceToPickup(BigDecimal value) { distanceToPickup = value; }
    public Integer getMaxDeliveries() { return maxDeliveries; }
    public void setMaxDeliveries(Integer value) { maxDeliveries = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
}
