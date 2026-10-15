package com.example.livraison.repository;

import com.example.livraison.entity.Incident;
import org.springframework.data.jpa.repository.*;
public interface IncidentRepository extends JpaRepository<Incident,Long>, JpaSpecificationExecutor<Incident> {}
