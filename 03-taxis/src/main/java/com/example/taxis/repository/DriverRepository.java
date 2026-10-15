package com.example.taxis.repository;

import com.example.taxis.entity.Driver;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;
public interface DriverRepository extends JpaRepository<Driver,Long>, JpaSpecificationExecutor<Driver> { Optional<Driver> findByEmailIgnoreCase(String email); Optional<Driver> findByVehicleId(Long vehicleId); }
