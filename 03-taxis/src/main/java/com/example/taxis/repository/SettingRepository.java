package com.example.taxis.repository;

import com.example.taxis.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SettingRepository extends JpaRepository<Setting,String> {}
