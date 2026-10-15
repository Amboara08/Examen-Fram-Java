package com.example.livraison.entity;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.persistence.*;
@Entity
@Table(name="parcel_records")
public class Parcel {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    private Long orderId;
    @Column(unique=true, length=2000)
    private String reference;
    @Column(length=2000)
    private String description;
    private BigDecimal weight;
    private BigDecimal volume;

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long value) { orderId = value; }
    public String getReference() { return reference; }
    public void setReference(String value) { reference = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { description = value; }
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal value) { weight = value; }
    public BigDecimal getVolume() { return volume; }
    public void setVolume(BigDecimal value) { volume = value; }
}
