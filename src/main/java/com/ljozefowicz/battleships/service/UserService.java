package com.ljozefowicz.battleships.service;

import com.ljozefowicz.battleships.dto.UserRegistrationDto;
import com.ljozefowicz.battleships.model.entity.User;

public interface UserService{

    User saveUser(UserRegistrationDto userRegistrationDto);

    User findByUsername(String username);

}
