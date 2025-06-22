package com.coditas.tool.management.system.dto.tool;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolRequestCreateDTO {
    @NotEmpty(message = "Tool request must contain at least one item.")
    private List<ToolRequestItemDTO> items;
}
