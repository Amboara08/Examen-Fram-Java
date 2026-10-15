package com.example.parking.repository;

import com.example.parking.entity.Reservation;
import org.springframework.data.jpa.repository.*;
public interface ReservationRepository extends JpaRepository<Reservation,Long>, JpaSpecificationExecutor<Reservation> {}
