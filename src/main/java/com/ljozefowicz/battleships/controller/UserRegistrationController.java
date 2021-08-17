package com.ljozefowicz.battleships.controller;

import com.ljozefowicz.battleships.dto.UserRegistrationDto;
import com.ljozefowicz.battleships.entity.User;
import com.ljozefowicz.battleships.service.UserService;
import com.ljozefowicz.battleships.validator.UserValidator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@AllArgsConstructor
@RequestMapping("/")
public class UserRegistrationController {

    private UserService userService;
    private UserValidator userValidator;

//    public UserRegistrationController(UserService userService) {
//        this.userService = userService;
//    }

//    @ModelAttribute("user")
//    public UserRegistrationDto userRegistrationDto(){
//        return new UserRegistrationDto();
//    }

    @GetMapping("/register")
    public String getRegisterPage(Model model){
        model.addAttribute("user", new UserRegistrationDto());
        return "registration";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") UserRegistrationDto userRegistrationDto, BindingResult bindingResult){

        userValidator.validate(userRegistrationDto, bindingResult);
//        User existing = userService.findByUsername(userRegistrationDto.getUsername());
//        if (existing != null) {
//            bindingResult.rejectValue("username", null, "User with this username already exists");
//        }

        if (bindingResult.hasErrors()) {
            return "registration";
        }


        userService.saveUser(userRegistrationDto);
        return "redirect:/register?success";
    }
}
