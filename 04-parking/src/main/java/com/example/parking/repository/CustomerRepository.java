package com.example.parking.repository;

import com.example.parking.entity.Customer;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;
public interface CustomerRepository extends JpaRepository<Customer,Long>, JpaSpecificationExecutor<Customer> {
    Optional<Customer> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
