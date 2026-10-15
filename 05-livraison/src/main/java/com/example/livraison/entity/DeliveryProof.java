package com.example.livraison.entity;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.persistence.*;
@Entity
@Table(name="deliveryproof_records")
public class DeliveryProof {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    private Long orderId;
    @Column(length=2000)
    private String recipient;
    @Column(length=2000)
    private String confirmationCode;
    @Column(length=2000)
    private String comment;
    private LocalDateTime deliveredAt;

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long value) { orderId = value; }
    public String getRecipient() { return recipient; }
    public void setRecipient(String value) { recipient = value; }
    public String getConfirmationCode() { return confirmationCode; }
    public void setConfirmationCode(String value) { confirmationCode = value; }
    public String getComment() { return comment; }
    public void setComment(String value) { comment = value; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime value) { deliveredAt = value; }
}
