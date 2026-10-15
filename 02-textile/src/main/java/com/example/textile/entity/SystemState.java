package com.example.textile.entity;

import jakarta.persistence.*;
@Entity public class SystemState {
 @Id private Long id=1L;
 private String name="verrou métier";
 public Long getId() {return id;}
}
