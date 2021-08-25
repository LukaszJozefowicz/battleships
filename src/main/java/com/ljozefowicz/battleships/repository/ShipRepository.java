package com.ljozefowicz.battleships.repository;

import com.ljozefowicz.battleships.model.entity.Ship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipRepository extends JpaRepository<Ship, Long> {
}
