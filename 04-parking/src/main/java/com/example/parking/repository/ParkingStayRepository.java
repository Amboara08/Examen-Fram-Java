package com.example.parking.repository;

import com.example.parking.entity.ParkingStay;
import org.springframework.data.jpa.repository.*;
public interface ParkingStayRepository extends JpaRepository<ParkingStay,Long>, JpaSpecificationExecutor<ParkingStay> {}
