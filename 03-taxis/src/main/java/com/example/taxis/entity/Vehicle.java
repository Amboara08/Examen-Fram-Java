package com.example.taxis.entity;

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
    @Column(length=2000)
    private String brand;
    @Column(length=2000)
    private String model;
    @Column(name="vehicle_year") private Integer year;
    private Integer capacity;
    private BigDecimal mileage;
    @Column(length=2000)
    private String status = "DISPONIBLE";

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public String getPlate() { return plate; }
    public void setPlate(String value) { plate = value; }
    public String getBrand() { return brand; }
    public void setBrand(String value) { brand = value; }
    public String getModel() { return model; }
    public void setModel(String value) { model = value; }
    public Integer getYear() { return year; }
    public void setYear(Integer value) { year = value; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer value) { capacity = value; }
    public BigDecimal getMileage() { return mileage; }
    public void setMileage(BigDecimal value) { mileage = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
}
