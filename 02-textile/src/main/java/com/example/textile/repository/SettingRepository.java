package com.example.textile.repository;

import com.example.textile.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SettingRepository extends JpaRepository<Setting,String> {}
