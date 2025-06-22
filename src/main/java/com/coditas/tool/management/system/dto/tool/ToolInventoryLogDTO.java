package com.coditas.tool.management.system.dto.tool;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ToolInventoryLogDTO {
    private String toolName;
    private String workplaceName;
    private String toolCribName;
    private String assignedBy;
    private Long quantityAssigned;
    private LocalDateTime assignedAt;
}
