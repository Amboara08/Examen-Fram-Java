package com.example.livraison.repository;

import com.example.livraison.entity.DeliveryOrder;
import org.springframework.data.jpa.repository.*;
public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrder,Long>, JpaSpecificationExecutor<DeliveryOrder> {}
