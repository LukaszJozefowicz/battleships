package com.ljozefowicz.battleships.repository;

import com.ljozefowicz.battleships.enums.Difficulty;
import com.ljozefowicz.battleships.enums.GameTurn;
import com.ljozefowicz.battleships.enums.ShipShape;
import com.ljozefowicz.battleships.model.entity.Settings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettingsRepository extends JpaRepository<Settings, Long> {
    Optional<Settings> findByDifficulty(Difficulty difficulty);
    Optional<Settings> findByDifficultyAndStartingPlayerAndShipShape(Difficulty difficulty, GameTurn startingPlayer, ShipShape shipShape);
}
