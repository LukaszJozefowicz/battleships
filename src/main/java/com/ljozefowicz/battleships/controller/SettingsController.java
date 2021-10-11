package com.ljozefowicz.battleships.controller;

import com.ljozefowicz.battleships.enums.Difficulty;
import com.ljozefowicz.battleships.model.entity.Settings;
import com.ljozefowicz.battleships.repository.SettingsRepository;
import com.ljozefowicz.battleships.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
@AllArgsConstructor
public class SettingsController {

    private final SettingsRepository settingsRepository;
    private final UserService userService;

    @GetMapping("/gameSettings")
    public String getSettingsPage(){
        return "settings";
    }

    @PostMapping("/saveSettings")
    public String saveSettings(String difficulty, Principal principal){
        Settings settings = Settings.builder()
                .difficulty(Difficulty.valueOf(difficulty))
                .build();

        //settingsRepository.save(settings);
        userService.saveUserSettings(principal.getName(), settings);

        return "redirect:/";
    }
}
