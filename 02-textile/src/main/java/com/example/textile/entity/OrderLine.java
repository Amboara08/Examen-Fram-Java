package com.example.textile.entity;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.persistence.*;
@Entity
@Table(name="orderline_records")
public class OrderLine {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    private Long orderId;
    private Long productId;
    private Integer quantity;
    @Column(length=2000)
    private String size;
    @Column(length=2000)
    private String color;
    private BigDecimal unitPrice;
    private Integer producedQuantity = 0;

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long value) { orderId = value; }
    public Long getProductId() { return productId; }
    public void setProductId(Long value) { productId = value; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer value) { quantity = value; }
    public String getSize() { return size; }
    public void setSize(String value) { size = value; }
    public String getColor() { return color; }
    public void setColor(String value) { color = value; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal value) { unitPrice = value; }
    public Integer getProducedQuantity() { return producedQuantity; }
    public void setProducedQuantity(Integer value) { producedQuantity = value; }
}
