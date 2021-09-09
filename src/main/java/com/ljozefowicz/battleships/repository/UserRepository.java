package com.ljozefowicz.battleships.repository;

import com.ljozefowicz.battleships.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query(nativeQuery = true,
            value = "SELECT CASE WHEN count(*) >= 1 THEN 'true' ELSE 'false' END " +
                    "FROM users WHERE username = ?1 AND password = ?2")
    Boolean checkIfUserExists(String username, String password);

    User getUserByUsernameAndPassword(String username, String password);

    Optional<User> findByUsername(String username);
    User findByEmail(String email);
}