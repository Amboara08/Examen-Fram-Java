package com.example.textile.repository;

import com.example.textile.entity.QualityControl;
import org.springframework.data.jpa.repository.*;
public interface QualityControlRepository extends JpaRepository<QualityControl,Long>, JpaSpecificationExecutor<QualityControl> {}
