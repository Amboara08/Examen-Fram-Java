package com.example.parking.repository;

import com.example.parking.entity.Vehicle;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;
public interface VehicleRepository extends JpaRepository<Vehicle,Long>, JpaSpecificationExecutor<Vehicle> {
    Optional<Vehicle> findFirstByCustomerId(Long customerId);
    Optional<Vehicle> findByPlateIgnoreCase(String plate);
}
