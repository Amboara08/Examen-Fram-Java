package com.example.livraison.repository;

import com.example.livraison.entity.DeliveryProof;
import org.springframework.data.jpa.repository.*;
public interface DeliveryProofRepository extends JpaRepository<DeliveryProof,Long>, JpaSpecificationExecutor<DeliveryProof> {}
