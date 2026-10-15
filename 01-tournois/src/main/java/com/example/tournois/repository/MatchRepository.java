package com.example.tournois.repository;

import com.example.tournois.entity.Match;
import org.springframework.data.jpa.repository.*;
public interface MatchRepository extends JpaRepository<Match,Long>, JpaSpecificationExecutor<Match> {}
