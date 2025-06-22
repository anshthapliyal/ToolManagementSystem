package com.coditas.tool.management.system.dto.user;

import com.coditas.tool.management.system.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserDTO {

    private Long id;

    @NotBlank(message = "Name cannot be empty.")
    private String name;

    @Email(message = "Invalid email format.")
    @NotBlank(message = "Email cannot be empty.")
    private String email;

    private String password;


    private List<Role> roles;

}
