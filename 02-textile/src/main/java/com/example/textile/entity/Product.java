package com.example.textile.entity;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.persistence.*;
@Entity
@Table(name="product_records")
public class Product {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    @Column(length=2000)
    private String name;
    @Column(unique=true, length=2000)
    private String reference;
    @Column(length=2000)
    private String description;
    private BigDecimal unitPrice;

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public String getName() { return name; }
    public void setName(String value) { name = value; }
    public String getReference() { return reference; }
    public void setReference(String value) { reference = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { description = value; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal value) { unitPrice = value; }
}
