package com.example.taxis.repository;

import com.example.taxis.entity.Vehicle;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;
public interface VehicleRepository extends JpaRepository<Vehicle,Long>, JpaSpecificationExecutor<Vehicle> { Optional<Vehicle> findByPlateIgnoreCase(String plate); }
