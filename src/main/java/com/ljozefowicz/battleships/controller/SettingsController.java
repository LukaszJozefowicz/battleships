package com.ljozefowicz.battleships.controller;

import com.ljozefowicz.battleships.dto.SettingsDto;
import com.ljozefowicz.battleships.dto.mapper.DtoMapper;
import com.ljozefowicz.battleships.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
@AllArgsConstructor
public class SettingsController {

    private final DtoMapper dtoMapper;
    private final UserService userService;

    @GetMapping("/gameSettings")
    public String getSettingsPage(Model model, Principal principal){
        SettingsDto settingsDto = dtoMapper.mapToSettingsDto(userService.getUserSettings(principal.getName()));

        model.addAttribute("settings", settingsDto);
        return "settings";
    }

    @PostMapping("/saveSettings")
    public String saveSettings(SettingsDto settings, Principal principal){

        userService.saveUserSettings(principal.getName(), settings);

        return "redirect:/gameSettings?success";
    }
}
