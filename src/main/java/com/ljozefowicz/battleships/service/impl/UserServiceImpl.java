package com.ljozefowicz.battleships.service.impl;

import com.ljozefowicz.battleships.dto.UserRegistrationDto;
import com.ljozefowicz.battleships.model.entity.Game;
import com.ljozefowicz.battleships.model.entity.Role;
import com.ljozefowicz.battleships.model.entity.Settings;
import com.ljozefowicz.battleships.model.entity.User;
import com.ljozefowicz.battleships.enums.UserRole;
import com.ljozefowicz.battleships.repository.SettingsRepository;
import com.ljozefowicz.battleships.repository.UserRepository;
import com.ljozefowicz.battleships.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

import static com.ljozefowicz.battleships.enums.UserRole.isBot;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SettingsRepository settingsRepository;
    private final BCryptPasswordEncoder encoder;

    @Override
    public User saveUser(UserRegistrationDto userRegistrationDto, UserRole userRole) {

        if (userRole.name().contains("BOT")){
            String randomIntString = Integer.toString(new Random().nextInt(1000) + 1000);
            userRegistrationDto.setUsername(userRole.getRoleName() + randomIntString);
            userRegistrationDto.setPassword(randomIntString);
            userRegistrationDto.setConfirmPassword(randomIntString);
            userRegistrationDto.setEmail(userRole.getRoleName() + randomIntString + "@" + randomIntString + ".pl");
            userRegistrationDto.setConfirmEmail(userRole.getRoleName() + randomIntString + "@" + randomIntString + ".pl");
        }

        User user = new User(userRegistrationDto.getUsername(),
                encoder.encode(userRegistrationDto.getPassword()),
                encoder.encode(userRegistrationDto.getConfirmPassword()),
                userRegistrationDto.getEmail(),
                userRegistrationDto.getConfirmEmail(),
                Arrays.asList(new Role(userRole.getId(), userRole)),
                settingsRepository.findById(1L).get()); //default settings
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public void deleteBotUserIfPresent(Game game){
        if(game.getPlayer2() != null) {
            if (isBot(game.getPlayer2().getUsername()))
                userRepository.deleteByUsername(game.getPlayer2().getUsername());
        }
    }

    @Override
    public User saveUserSettings(String username, Settings settings){
        User user = findByUsername(username).get();

        settingsRepository.findByDifficulty(settings.getDifficulty())
                .ifPresentOrElse(s -> user.setSettings(s),
                () -> {
                    Settings newSettings = settingsRepository.save(settings);
                    user.setSettings(newSettings);
                });
        return userRepository.save(user);
    }
}
