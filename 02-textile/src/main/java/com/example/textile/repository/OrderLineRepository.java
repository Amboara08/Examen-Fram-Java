package com.example.textile.repository;

import com.example.textile.entity.OrderLine;
import org.springframework.data.jpa.repository.*;
public interface OrderLineRepository extends JpaRepository<OrderLine,Long>, JpaSpecificationExecutor<OrderLine> {}
