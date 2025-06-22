package com.coditas.tool.management.system.dto.tool;

import com.coditas.tool.management.system.constant.ToolCategory;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolInventoryDTO {
    private Long toolId;
    private String toolName;
    private Long totalQuantity;
    private Long availableQuantity;
    private String toolCribName;
    private Long toolCribId;
    private Long minimumThreshold;
    private String toolImageUrl;
    private Long fineAmount;
    private Long brokenQuantity;
    private Integer returnPeriod;
    private ToolCategory toolCategory;
    private Boolean isPerishable;
}
