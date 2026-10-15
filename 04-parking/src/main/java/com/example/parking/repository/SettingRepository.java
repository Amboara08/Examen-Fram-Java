package com.example.parking.repository;

import com.example.parking.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SettingRepository extends JpaRepository<Setting,String> {}
