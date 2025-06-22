package com.coditas.tool.management.system.dto.tool;

import com.coditas.tool.management.system.constant.RequestStatus;
import com.coditas.tool.management.system.constant.ReturnStatus;
import com.coditas.tool.management.system.constant.ToolCategory;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ToolRequestItemDTO {
    private Long workerId;
    private Long toolId;
    private Long requestItemId;
    private String toolName;
    private Long reqQuantity;
    private RequestStatus approvalStatus;
    private ReturnStatus returnStatus;
    private LocalDateTime requestDate;
    private LocalDateTime returnDate;
    private String workerName;
    private Long fine;

    private Boolean isPerishable;
    private ToolCategory toolCategory;

}

