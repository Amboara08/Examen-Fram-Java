package com.example.parking.repository;

import com.example.parking.entity.Subscription;
import org.springframework.data.jpa.repository.*;
public interface SubscriptionRepository extends JpaRepository<Subscription,Long>, JpaSpecificationExecutor<Subscription> {}
