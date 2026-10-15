package com.example.textile.entity;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.persistence.*;
@Entity
@Table(name="productionstep_records")
public class ProductionStep {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    private Long orderId;
    @Column(length=2000)
    private String type = "COUPE";
    @Column(length=2000)
    private String responsible;
    private LocalDateTime plannedStart;
    private LocalDateTime plannedEnd;
    @Column(length=2000)
    private String comment;
    @Column(length=2000)
    private String status = "PLANIFIEE";
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer quantity = 0;

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long value) { orderId = value; }
    public String getType() { return type; }
    public void setType(String value) { type = value; }
    public String getResponsible() { return responsible; }
    public void setResponsible(String value) { responsible = value; }
    public LocalDateTime getPlannedStart() { return plannedStart; }
    public void setPlannedStart(LocalDateTime value) { plannedStart = value; }
    public LocalDateTime getPlannedEnd() { return plannedEnd; }
    public void setPlannedEnd(LocalDateTime value) { plannedEnd = value; }
    public String getComment() { return comment; }
    public void setComment(String value) { comment = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime value) { startedAt = value; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime value) { completedAt = value; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer value) { quantity = value; }
}
