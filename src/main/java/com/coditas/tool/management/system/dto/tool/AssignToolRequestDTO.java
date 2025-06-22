package com.coditas.tool.management.system.dto.tool;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AssignToolRequestDTO {

    @NotBlank(message = "Workplace cannot be null.")
    private Long workplaceId;

    @NotBlank(message = "Tool cannot be null.")
    private Long toolId;

    @Positive(message = "Quantity being transferred must be more than zero.")
    private Long quantity;
}
