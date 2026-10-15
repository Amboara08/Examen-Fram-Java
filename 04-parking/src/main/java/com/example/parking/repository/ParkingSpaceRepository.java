package com.example.parking.repository;

import com.example.parking.entity.ParkingSpace;
import org.springframework.data.jpa.repository.*;
public interface ParkingSpaceRepository extends JpaRepository<ParkingSpace,Long>, JpaSpecificationExecutor<ParkingSpace> {}
