package com.example.taxis.repository;

import com.example.taxis.entity.Ride;
import org.springframework.data.jpa.repository.*;
public interface RideRepository extends JpaRepository<Ride,Long>, JpaSpecificationExecutor<Ride> {}
