package com.ljozefowicz.battleships.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegistrationDto {

    @NotEmpty
    private String username;

    @NotEmpty
    private String password;

    @Email
    @NotEmpty
    private String email;

    @Email
    @NotEmpty
    private String confirmEmail;

    @NotEmpty
    private String confirmPassword;
}
