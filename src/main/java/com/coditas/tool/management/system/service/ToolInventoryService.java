package com.coditas.tool.management.system.service;

import com.coditas.tool.management.system.dto.tool.AssignToolRequestDTO;
import com.coditas.tool.management.system.dto.sharedResponse.SuccessResponse;
import com.coditas.tool.management.system.dto.tool.ToolInventoryDTO;
import org.springframework.data.domain.Page;

public interface ToolInventoryService {
    SuccessResponse assignToolToWorkplace(AssignToolRequestDTO request);

    Page<ToolInventoryDTO> getToolInventoryForToolCrib(Long toolCribId, int page,
                                                       int size, boolean filterLowStock);
}
