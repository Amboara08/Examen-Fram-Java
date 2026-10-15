package com.example.tournois.repository;

import com.example.tournois.entity.Team;
import org.springframework.data.jpa.repository.*;
public interface TeamRepository extends JpaRepository<Team,Long>, JpaSpecificationExecutor<Team> {}
