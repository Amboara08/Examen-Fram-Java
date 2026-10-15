package com.example.taxis.entity;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.persistence.*;
@Entity
@Table(name="ride_records")
public class Ride {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    private Long clientId;
    @Column(length=2000)
    private String departure;
    @Column(length=2000)
    private String destination;
    private BigDecimal distance;
    private LocalDateTime scheduledAt;
    private Integer passengers;
    private BigDecimal fare;
    private Long driverId;
    private Long vehicleId;
    @Column(length=2000)
    private String status = "EN_ATTENTE";
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long value) { clientId = value; }
    public String getDeparture() { return departure; }
    public void setDeparture(String value) { departure = value; }
    public String getDestination() { return destination; }
    public void setDestination(String value) { destination = value; }
    public BigDecimal getDistance() { return distance; }
    public void setDistance(BigDecimal value) { distance = value; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime value) { scheduledAt = value; }
    public Integer getPassengers() { return passengers; }
    public void setPassengers(Integer value) { passengers = value; }
    public BigDecimal getFare() { return fare; }
    public void setFare(BigDecimal value) { fare = value; }
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long value) { driverId = value; }
    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long value) { vehicleId = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime value) { startedAt = value; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime value) { completedAt = value; }
}
