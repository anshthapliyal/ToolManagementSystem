package com.coditas.tool.management.system.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MgrUpdateReqDTO {

    @NotBlank(message = "Please enter some name.")
    private String name;

    @Email(message = "Please enter a valid email.")
    @NotBlank(message = "Email cannot be empty")
    private String email;
}
