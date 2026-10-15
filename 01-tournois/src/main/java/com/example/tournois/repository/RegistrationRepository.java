package com.example.tournois.repository;

import com.example.tournois.entity.Registration;
import org.springframework.data.jpa.repository.*;
public interface RegistrationRepository extends JpaRepository<Registration,Long>, JpaSpecificationExecutor<Registration> {}
