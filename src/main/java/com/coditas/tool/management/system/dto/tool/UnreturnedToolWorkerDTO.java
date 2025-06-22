package com.coditas.tool.management.system.dto.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UnreturnedToolWorkerDTO {
    private String workerName;
    private String workerEmail;
    private String workerImageUrl;
    private String toolName;
    private Long requestedQuantity;
    private Long returnedQuantity;
    private LocalDateTime dueDate;
    private Long toolRequestItemId;
}
