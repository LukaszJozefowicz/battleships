package com.ljozefowicz.battleships.service;

import com.ljozefowicz.battleships.dto.SettingsDto;
import com.ljozefowicz.battleships.dto.UserRegistrationDto;
import com.ljozefowicz.battleships.enums.UserRole;
import com.ljozefowicz.battleships.model.entity.Game;
import com.ljozefowicz.battleships.model.entity.Settings;
import com.ljozefowicz.battleships.model.entity.User;

import java.util.Optional;

public interface UserService{

    User saveUser(UserRegistrationDto userRegistrationDto, UserRole userRole, User masterUser);

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    void deleteBotUserIfPresent(Game game);

    User saveUserSettings(String username, SettingsDto settingsDto);
    Settings getUserSettings(String username);

}
