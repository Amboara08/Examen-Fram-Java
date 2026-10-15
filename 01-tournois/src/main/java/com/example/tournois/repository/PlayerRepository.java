package com.example.tournois.repository;

import com.example.tournois.entity.Player;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findByTeamIdOrderByNumberAsc(Long teamId);
    boolean existsByTeamIdAndNumber(Long teamId, Integer number);
    void deleteByTeamId(Long teamId);
}
