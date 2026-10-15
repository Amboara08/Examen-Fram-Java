package com.example.livraison.repository;

import com.example.livraison.entity.Deliverer;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;
public interface DelivererRepository extends JpaRepository<Deliverer,Long>, JpaSpecificationExecutor<Deliverer> {
    Optional<Deliverer> findByEmailIgnoreCase(String email);
}
