package com.ljozefowicz.battleships.service.impl;

import com.ljozefowicz.battleships.dto.UserRegistrationDto;
import com.ljozefowicz.battleships.model.entity.Role;
import com.ljozefowicz.battleships.model.entity.User;
import com.ljozefowicz.battleships.enums.UserRole;
import com.ljozefowicz.battleships.repository.UserRepository;
import com.ljozefowicz.battleships.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private BCryptPasswordEncoder encoder;

    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @Override
    public boolean checkCredentials(String login, String password) {
        return userRepository.checkIfUserExists(login, password);

    }


    @Override
    public User saveUser(UserRegistrationDto userRegistrationDto) {
        User user = new User(userRegistrationDto.getUsername(),
                encoder.encode(userRegistrationDto.getPassword()),
                encoder.encode(userRegistrationDto.getConfirmPassword()),
                userRegistrationDto.getEmail(),
                userRegistrationDto.getConfirmEmail(),
                Arrays.asList(new Role(1L, UserRole.ROLE_USER)));
        return userRepository.save(user);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
}
