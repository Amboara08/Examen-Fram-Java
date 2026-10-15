package com.example.taxis.entity;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.persistence.*;
@Entity
@Table(name="driver_records")
public class Driver {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    @Column(length=2000)
    private String name;
    @Column(length=2000)
    private String phone;
    @Column(unique=true,length=2000) private String email;
    @Column(name="password_hash",length=100) private String passwordHash;
    @Column(unique=true, length=2000)
    private String licenseNumber;
    private LocalDate licenseExpiry;
    private LocalDate hiredAt;
    private Long vehicleId;
    private BigDecimal distanceToPickup;
    @Column(length=2000)
    private String status = "DISPONIBLE";

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public String getName() { return name; }
    public void setName(String value) { name = value; }
    public String getPhone() { return phone; }
    public void setPhone(String value) { phone = value; }
    public String getEmail(){return email;} public void setEmail(String value){email=value;}
    public String getPasswordHash(){return passwordHash;} public void setPasswordHash(String value){passwordHash=value;}
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String value) { licenseNumber = value; }
    public LocalDate getLicenseExpiry() { return licenseExpiry; }
    public void setLicenseExpiry(LocalDate value) { licenseExpiry = value; }
    public LocalDate getHiredAt() { return hiredAt; }
    public void setHiredAt(LocalDate value) { hiredAt = value; }
    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long value) { vehicleId = value; }
    public BigDecimal getDistanceToPickup() { return distanceToPickup; }
    public void setDistanceToPickup(BigDecimal value) { distanceToPickup = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
}
