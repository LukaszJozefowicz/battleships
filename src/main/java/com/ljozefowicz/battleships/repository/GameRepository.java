package com.ljozefowicz.battleships.repository;

import com.ljozefowicz.battleships.enums.GameState;
import com.ljozefowicz.battleships.model.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findByPlayer1_idAndGameStateNot(Long id, GameState gameState);
    Optional<Game> findByPlayer2_idAndGameStateNot(Long id, GameState gameState);
    Optional<Game> findByPlayer1_idOrPlayer2_id(Long id, Long id2);
}
