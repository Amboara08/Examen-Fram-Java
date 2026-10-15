package com.example.textile.repository;

import com.example.textile.entity.SystemState;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.Optional;
public interface SystemStateRepository extends JpaRepository<SystemState,Long> {
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select s from SystemState s where s.id=1") Optional<SystemState> lock();
}
