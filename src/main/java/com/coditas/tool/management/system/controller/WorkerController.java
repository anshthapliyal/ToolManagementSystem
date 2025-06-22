package com.coditas.tool.management.system.controller;

import com.coditas.tool.management.system.constant.ToolCategory;
import com.coditas.tool.management.system.dto.sharedResponse.SuccessResponse;
import com.coditas.tool.management.system.dto.tool.ToolInventoryDTO;
import com.coditas.tool.management.system.dto.tool.ToolRequestCreateDTO;
import com.coditas.tool.management.system.dto.tool.ToolRequestItemDTO;
import com.coditas.tool.management.system.service.InventoryService;
import com.coditas.tool.management.system.service.ToolRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.coditas.tool.management.system.constant.AuthorityConstant.*;

@RestController
@RequestMapping("/worker")
@Validated
public class WorkerController {

    private final InventoryService inventoryService;
    private final ToolRequestService toolRequestService;

    @Autowired
    public WorkerController(InventoryService inventoryService, ToolRequestService toolRequestService) {
        this.inventoryService = inventoryService;
        this.toolRequestService = toolRequestService;
    }

    @GetMapping("/inventory")
    @PreAuthorize("hasAnyRole('" + OWNER + "','" + FACILITYMANAGER + "','" + WORKPLACEMANAGER + "','" + WORKER + "')")
    public ResponseEntity<Page<ToolInventoryDTO>> viewInventory(
            Authentication authentication,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean isPerishable,
            @RequestParam(required = false) List<ToolCategory> category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
    ) {
        String email = authentication.getName();
        Page<ToolInventoryDTO> inventoryPage = inventoryService.getInventoryForWorker(
                email, name, isPerishable, category, minPrice, maxPrice, page, size
        );
        return ResponseEntity.ok(inventoryPage);
    }


    @Operation(
            summary = "Request tools from tool crib",
            description = "Allows a worker to request one or more tools from their own workplace's tool crib. "
                    + "Special tools will require approval from the Workplace Manager, while normal tools will be handled by the Tool Crib Manager.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tool request submitted successfully."),
                    @ApiResponse(responseCode = "400", description = "Invalid input or tool request exceeds " +
                            "available quantity"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Access denied")
            }
    )
    @PostMapping("/request-tools")
    @PreAuthorize("hasRole('" + WORKER + "')")
    public ResponseEntity<SuccessResponse> requestTools(
            @Valid @RequestBody ToolRequestCreateDTO requestDTO,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(toolRequestService.createToolRequest(email, requestDTO));

    }

    @Operation(summary = "Get logged-in worker's tool requests")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched tool requests"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/my-requests")
    @PreAuthorize("hasRole('" + WORKER + "')")
    public ResponseEntity<Page<ToolRequestItemDTO>> getMyToolRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String toolName,
            @RequestParam(required = false) String approvalStatus
    ) {
        Page<ToolRequestItemDTO> requestItems = toolRequestService.getToolRequestsByWorker(toolName, approvalStatus, page, size);
        return ResponseEntity.ok(requestItems);
    }
}
