package com.example.textile.entity;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.persistence.*;
@Entity
@Table(name="textileorder_records")
public class TextileOrder {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    @Column(unique=true, length=2000)
    private String number;
    private Long clientId;
    private LocalDate orderDate;
    private LocalDate deliveryDate;
    @Column(length=2000)
    private String priority = "NORMALE";
    @Column(length=2000)
    private String status = "COMMANDE";
    private LocalDateTime deliveredAt;

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public String getNumber() { return number; }
    public void setNumber(String value) { number = value; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long value) { clientId = value; }
    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate value) { orderDate = value; }
    public LocalDate getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDate value) { deliveryDate = value; }
    public String getPriority() { return priority; }
    public void setPriority(String value) { priority = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime value) { deliveredAt = value; }
}
