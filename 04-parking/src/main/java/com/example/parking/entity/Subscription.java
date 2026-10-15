package com.example.parking.entity;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.persistence.*;
@Entity
@Table(name="subscription_records")
public class Subscription {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    private Long customerId;
    private Long vehicleId;
    @Column(length=2000)
    private String type = "JOURNALIER";
    private LocalDate startDate;
    private LocalDate endDate;
    @Column(length=2000)
    private String status = "ACTIF";

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long value) { customerId = value; }
    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long value) { vehicleId = value; }
    public String getType() { return type; }
    public void setType(String value) { type = value; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate value) { startDate = value; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate value) { endDate = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
}
