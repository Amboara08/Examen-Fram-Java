package com.example.tournois.config;

import com.example.tournois.entity.*;
import com.example.tournois.repository.*;
import com.example.tournois.service.impl.ApplicationServiceImpl;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.*;
@Configuration public class DatabaseInitializer {
 @Bean @org.springframework.core.annotation.Order(0) ApplicationRunner initialise(SystemStateRepository state,SettingRepository settings) {return args->{if(!state.existsById(1L))state.save(new SystemState());ApplicationServiceImpl.DEFAULTS.forEach((name,value)->{if(!settings.existsById(name))settings.save(new Setting(name,value));});};}
}
