package com.ljozefowicz.battleships.repository;

import com.ljozefowicz.battleships.model.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
}
