package com.ljozefowicz.battleships.validator;

import com.ljozefowicz.battleships.dto.UserRegistrationDto;
import com.ljozefowicz.battleships.enums.UserRole;
import com.ljozefowicz.battleships.model.entity.User;
import com.ljozefowicz.battleships.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
@AllArgsConstructor
@ConfigurationProperties(prefix = "messages.properties")
public class UserValidator implements Validator {

    private final UserService userService;

    @Override
    public boolean supports(Class<?> aClass) {
        return User.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        UserRegistrationDto user = (UserRegistrationDto) o;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "NotEmpty");
        if (user.getUsername().length() < 4 || user.getUsername().length() > 16) {
            errors.rejectValue("username", "Size.user.username");
        }
        if (UserRole.isBot(user.getUsername())) {
            errors.rejectValue("username", "Forbidden.user.username");
        }

        userService.findByUsername(user.getUsername())
                .ifPresent(u -> errors.rejectValue("username", "Duplicate.user.username"));

        userService.findByEmail(user.getEmail())
                .ifPresent(u -> errors.rejectValue("email", "Duplicate.user.email"));

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "NotEmpty");
        if (user.getPassword().length() < 8 || user.getPassword().length() > 32) {
            errors.rejectValue("password", "Size.user.password");
        }

        if (!user.getConfirmPassword().equals(user.getPassword())) {
            errors.rejectValue("confirmPassword", "Diff.user.passwordConfirm");
        }

        if (!user.getConfirmEmail().equals(user.getEmail())) {
            errors.rejectValue("confirmEmail", "Diff.user.emailConfirm");
        }
    }
}
