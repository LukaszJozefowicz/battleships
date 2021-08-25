package com.ljozefowicz.battleships.repository;

import com.ljozefowicz.battleships.model.Field;
import com.ljozefowicz.battleships.model.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {
}
