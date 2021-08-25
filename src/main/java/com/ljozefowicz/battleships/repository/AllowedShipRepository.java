package com.ljozefowicz.battleships.repository;

import com.ljozefowicz.battleships.model.entity.AllowedShip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllowedShipRepository extends JpaRepository<AllowedShip, Long> {
}
