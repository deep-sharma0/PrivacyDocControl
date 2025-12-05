package com.privacydoccontrol.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRegistrationDto {
    @Email @NotBlank
    private String email;

    @Size(min = 6) @NotBlank
    private String password;

    @Size(min = 6) @NotBlank
    private String confirmPassword;

    // getters & setters
}
