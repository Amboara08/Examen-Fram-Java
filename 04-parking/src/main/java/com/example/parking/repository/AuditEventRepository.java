package com.example.parking.repository;

import com.example.parking.entity.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AuditEventRepository extends JpaRepository<AuditEvent,Long> { List<AuditEvent> findByResourceAndResourceIdOrderByOccurredAtDesc(String resource,Long resourceId); }
