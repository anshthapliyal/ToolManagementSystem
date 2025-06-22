package com.coditas.tool.management.system.service;

import com.coditas.tool.management.system.dto.sharedResponse.SuccessResponse;
import com.coditas.tool.management.system.dto.tool.ToolRequestCreateDTO;
import com.coditas.tool.management.system.dto.tool.ToolRequestItemDTO;
import com.coditas.tool.management.system.dto.tool.ToolReturnRequestDTO;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface ToolRequestService {

    public Page<ToolRequestItemDTO> getSpecialRequestsForManager(
            int page, int size,
            String search, List<String> fields,
            LocalDateTime startDateTime, LocalDateTime endDateTime);

    public SuccessResponse decideSpecialRequest(Long itemId, boolean approve)
            throws BadRequestException;

    public SuccessResponse createToolRequest(String email, ToolRequestCreateDTO requestDTO);

    Page<ToolRequestItemDTO> getToolRequestsByWorker(String toolName, String approvalStatus, int page, int size);

    SuccessResponse decideNormalToolRequest(Long itemId, boolean approve) throws BadRequestException;

    Page<ToolRequestItemDTO> getAllToolRequestsForCribManager(
            int page, int size,
            String search,
            List<String> fields,
            LocalDateTime startDateTime, LocalDateTime endDateTime);

    SuccessResponse returnTool(ToolReturnRequestDTO requestDTO) throws BadRequestException;

    SuccessResponse getUnreturnedToolWorkers();
}
