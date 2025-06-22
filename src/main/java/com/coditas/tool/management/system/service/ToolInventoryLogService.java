package com.coditas.tool.management.system.service;

import com.coditas.tool.management.system.dto.tool.ToolInventoryLogDTO;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public interface ToolInventoryLogService {
    Page<ToolInventoryLogDTO> getLogs(int page, int size,
                                      LocalDateTime minDateTime, LocalDateTime maxDateTime);
}
