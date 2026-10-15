package com.example.taxis.repository;

import com.example.taxis.entity.Client;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;
public interface ClientRepository extends JpaRepository<Client,Long>, JpaSpecificationExecutor<Client> { Optional<Client> findByEmailIgnoreCase(String email); }
