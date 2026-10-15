package com.example.textile.entity;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.persistence.*;
@Entity
@Table(name="qualitycontrol_records")
public class QualityControl {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    private Long orderId;
    @Column(length=2000)
    private String result = "CONFORME";
    @Column(length=2000)
    private String responsible;
    @Column(length=2000)
    private String comment;
    private LocalDateTime checkedAt;

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long value) { orderId = value; }
    public String getResult() { return result; }
    public void setResult(String value) { result = value; }
    public String getResponsible() { return responsible; }
    public void setResponsible(String value) { responsible = value; }
    public String getComment() { return comment; }
    public void setComment(String value) { comment = value; }
    public LocalDateTime getCheckedAt() { return checkedAt; }
    public void setCheckedAt(LocalDateTime value) { checkedAt = value; }
}
