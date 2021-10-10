package com.ljozefowicz.battleships.service;

import com.ljozefowicz.battleships.dto.UserRegistrationDto;
import com.ljozefowicz.battleships.enums.UserRole;
import com.ljozefowicz.battleships.model.entity.Game;
import com.ljozefowicz.battleships.model.entity.User;

import java.util.Optional;

public interface UserService{

    User saveUser(UserRegistrationDto userRegistrationDto, UserRole userRole);

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    void deleteBotUserIfPresent(Game game);

}
