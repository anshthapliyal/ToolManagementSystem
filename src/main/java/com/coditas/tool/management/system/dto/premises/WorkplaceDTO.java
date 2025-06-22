package com.coditas.tool.management.system.dto.premises;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkplaceDTO {

    @NotBlank(message = "Name of workplace cannot be empty.")
    private String name;
    private String workplaceManagerEmail;
    private String workplaceManagerName;
}
