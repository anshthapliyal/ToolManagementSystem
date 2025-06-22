package com.coditas.tool.management.system.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtRequest {

    @NotBlank(message = "Email must not be blank.")
    @Email(message = "Please enter a valid email.")
    private String email;

    @NotBlank(message = "Password must not be blank.")
    private String password;
}
