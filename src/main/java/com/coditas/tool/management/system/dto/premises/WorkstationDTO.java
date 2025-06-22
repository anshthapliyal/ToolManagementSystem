package com.coditas.tool.management.system.dto.premises;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkstationDTO {

    private Long id;

    private String name;

    @NotBlank(message = "Worker Email cannot be empty!")
    private String workerEmail;

    private String workerName;
}
