package com.example.tournois.repository;

import com.example.tournois.entity.Tournament;
import org.springframework.data.jpa.repository.*;
public interface TournamentRepository extends JpaRepository<Tournament,Long>, JpaSpecificationExecutor<Tournament> {}
