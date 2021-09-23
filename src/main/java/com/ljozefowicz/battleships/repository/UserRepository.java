package com.ljozefowicz.battleships.repository;

import com.ljozefowicz.battleships.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    User getUserByUsernameAndPassword(String username, String password);

    Optional<User> findByUsername(String username);
    User findByEmail(String email);
}
