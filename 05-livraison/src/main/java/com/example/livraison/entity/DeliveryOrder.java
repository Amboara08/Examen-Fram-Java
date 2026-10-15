package com.example.livraison.entity;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.persistence.*;
@Entity
@Table(name="deliveryorder_records")
public class DeliveryOrder {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    @Column(unique=true, length=2000)
    private String number;
    private Long clientId;
    @Column(length=2000)
    private String pickupAddress;
    @Column(length=2000)
    private String deliveryAddress;
    private BigDecimal weight;
    private BigDecimal volume;
    @Column(length=2000)
    private String priority = "NORMALE";
    private BigDecimal distance;
    @Column(length=2000)
    private String zone;
    private LocalDateTime expectedAt;
    private BigDecimal price;
    private Long delivererId;
    @Column(length=2000)
    private String status = "CREEE";
    private LocalDateTime createdAt;
    private LocalDateTime deliveredAt;

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public String getNumber() { return number; }
    public void setNumber(String value) { number = value; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long value) { clientId = value; }
    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String value) { pickupAddress = value; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String value) { deliveryAddress = value; }
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal value) { weight = value; }
    public BigDecimal getVolume() { return volume; }
    public void setVolume(BigDecimal value) { volume = value; }
    public String getPriority() { return priority; }
    public void setPriority(String value) { priority = value; }
    public BigDecimal getDistance() { return distance; }
    public void setDistance(BigDecimal value) { distance = value; }
    public String getZone() { return zone; }
    public void setZone(String value) { zone = value; }
    public LocalDateTime getExpectedAt() { return expectedAt; }
    public void setExpectedAt(LocalDateTime value) { expectedAt = value; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal value) { price = value; }
    public Long getDelivererId() { return delivererId; }
    public void setDelivererId(Long value) { delivererId = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { createdAt = value; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime value) { deliveredAt = value; }
}
