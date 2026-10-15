package com.example.parking.entity;

import jakarta.persistence.*;
@Entity public class Setting {
 @Id private String name;
 @Column(name="setting_value") private double value;
 public Setting() {} public Setting(String name,double value) {this.name=name;this.value=value;}
 public String getName(){return name;} public double getValue(){return value;}
}
