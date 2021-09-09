package com.ljozefowicz.battleships.repository;

import com.ljozefowicz.battleships.model.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findByPlayer1_id(Long id);
    Optional<Game> findByPlayer2_id(Long id);
}
