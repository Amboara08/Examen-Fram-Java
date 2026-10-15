package com.example.textile.entity;

import java.time.*;
import java.math.*;
import java.util.*;
import jakarta.persistence.*;
@Entity
@Table(name="client_records")
public class Client {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    @Column(length=2000)
    private String name;
    @Column(length=2000)
    private String email;
    @Column(length=2000)
    private String phone;
    @Column(length=2000)
    private String address;

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public String getName() { return name; }
    public void setName(String value) { name = value; }
    public String getEmail() { return email; }
    public void setEmail(String value) { email = value; }
    public String getPhone() { return phone; }
    public void setPhone(String value) { phone = value; }
    public String getAddress() { return address; }
    public void setAddress(String value) { address = value; }
}
