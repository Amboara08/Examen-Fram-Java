package com.example.parking.entity;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.persistence.*;
@Entity
@Table(name="vehicle_records")
public class Vehicle {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    @Column(unique=true, length=2000)
    private String plate;
    private Long customerId;
    @Column(length=2000)
    private String type = "VOITURE";
    @Column(length=120)
    private String brand;
    @Column(length=120)
    private String model;

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public String getPlate() { return plate; }
    public void setPlate(String value) { plate = value; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long value) { customerId = value; }
    public String getType() { return type; }
    public void setType(String value) { type = value; }
    public String getBrand() { return brand; }
    public void setBrand(String value) { brand = value; }
    public String getModel() { return model; }
    public void setModel(String value) { model = value; }
}
