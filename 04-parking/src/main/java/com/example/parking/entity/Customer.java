package com.example.parking.entity;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.persistence.*;
@Entity
@Table(name="customer_records", uniqueConstraints=@UniqueConstraint(name="uk_customer_email", columnNames="email"))
public class Customer {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    @Column(length=2000)
    private String name;
    @Column(length=2000)
    private String email;
    @Column(length=2000)
    private String phone;
    @Column(name="password_hash", length=100)
    private String passwordHash;
    @Column(length=2000)
    private String type = "STANDARD";

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public String getName() { return name; }
    public void setName(String value) { name = value; }
    public String getEmail() { return email; }
    public void setEmail(String value) { email = value; }
    public String getPhone() { return phone; }
    public void setPhone(String value) { phone = value; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String value) { passwordHash = value; }
    public String getType() { return type; }
    public void setType(String value) { type = value; }
}
