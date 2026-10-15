package com.example.textile.repository;

import com.example.textile.entity.ProductionStep;
import org.springframework.data.jpa.repository.*;
public interface ProductionStepRepository extends JpaRepository<ProductionStep,Long>, JpaSpecificationExecutor<ProductionStep> {}
