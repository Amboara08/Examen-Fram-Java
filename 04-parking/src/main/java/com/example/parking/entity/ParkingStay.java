package com.example.parking.entity;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.persistence.*;
@Entity
@Table(name="parkingstay_records")
public class ParkingStay {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    private Long customerId;
    private Long vehicleId;
    private Long spaceId;
    private Long reservationId;
    private LocalDateTime enteredAt;
    private LocalDateTime expectedExit;
    private LocalDateTime exitedAt;
    private BigDecimal amount;
    @Column(length=2000)
    private String status = "EN_COURS";

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long value) { customerId = value; }
    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long value) { vehicleId = value; }
    public Long getSpaceId() { return spaceId; }
    public void setSpaceId(Long value) { spaceId = value; }
    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long value) { reservationId = value; }
    public LocalDateTime getEnteredAt() { return enteredAt; }
    public void setEnteredAt(LocalDateTime value) { enteredAt = value; }
    public LocalDateTime getExpectedExit() { return expectedExit; }
    public void setExpectedExit(LocalDateTime value) { expectedExit = value; }
    public LocalDateTime getExitedAt() { return exitedAt; }
    public void setExitedAt(LocalDateTime value) { exitedAt = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { amount = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
}
