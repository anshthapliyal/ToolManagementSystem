package com.coditas.tool.management.system.dto.tool;

import com.coditas.tool.management.system.constant.ToolCategory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ToolFilterRequestDTO {
    private String name;
    private Boolean isPerishable;
    private ToolCategory category;
    private Double minPrice;
    private Double maxPrice;
}
