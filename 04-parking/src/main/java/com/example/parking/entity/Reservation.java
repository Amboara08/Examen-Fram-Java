package com.example.parking.entity;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.persistence.*;
@Entity
@Table(name="reservation_records")
public class Reservation {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    private Long customerId;
    private Long vehicleId;
    private Long spaceId;
    private LocalDateTime arrival;
    private LocalDateTime departure;
    @Column(length=2000)
    private String status = "CONFIRMEE";

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long value) { customerId = value; }
    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long value) { vehicleId = value; }
    public Long getSpaceId() { return spaceId; }
    public void setSpaceId(Long value) { spaceId = value; }
    public LocalDateTime getArrival() { return arrival; }
    public void setArrival(LocalDateTime value) { arrival = value; }
    public LocalDateTime getDeparture() { return departure; }
    public void setDeparture(LocalDateTime value) { departure = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
}
