package com.example.livraison.entity;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.persistence.*;
@Entity
@Table(name="incident_records")
public class Incident {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    private Long orderId;
    @Column(length=2000)
    private String type = "CLIENT_ABSENT";
    @Column(length=2000)
    private String description;
    @Column(length=2000)
    private String resolution;
    private Long delivererId;
    private LocalDateTime occurredAt;

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long value) { orderId = value; }
    public String getType() { return type; }
    public void setType(String value) { type = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { description = value; }
    public String getResolution() { return resolution; }
    public void setResolution(String value) { resolution = value; }
    public Long getDelivererId() { return delivererId; }
    public void setDelivererId(Long value) { delivererId = value; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime value) { occurredAt = value; }
}
