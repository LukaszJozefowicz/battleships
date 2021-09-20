package com.ljozefowicz.battleships.repository;

import com.ljozefowicz.battleships.model.entity.Ship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipRepository extends JpaRepository<Ship, Long> {

    List<Ship> findAllByBoard_id(Long id);
    void deleteAllByBoard_id(Long id);
    Ship findByBoard_idAndFieldsLike(Long id, String field);
}
