package com.coditas.tool.management.system.controller;

import com.coditas.tool.management.system.constant.ToolCategory;
import com.coditas.tool.management.system.dto.sharedResponse.SuccessResponse;
import com.coditas.tool.management.system.dto.tool.ToolInventoryDTO;
import com.coditas.tool.management.system.dto.tool.ToolRequestItemDTO;
import com.coditas.tool.management.system.dto.tool.ToolReturnRequestDTO;
import com.coditas.tool.management.system.service.InventoryService;
import com.coditas.tool.management.system.service.ToolRequestService;
import com.coditas.tool.management.system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static com.coditas.tool.management.system.constant.AuthorityConstant.TOOLCRIBMANAGER;

@RestController
@RequestMapping("/tool-crib-manager")
@Validated
public class ToolCribManagerController {

    private final ToolRequestService toolRequestService;
    private final InventoryService inventoryService;
    private final UserService userService;

    @Autowired
    public ToolCribManagerController(ToolRequestService toolRequestService, InventoryService inventoryService, UserService userService) {
        this.toolRequestService = toolRequestService;
        this.inventoryService = inventoryService;
        this.userService = userService;
    }

    @GetMapping("/all-requests")
    @PreAuthorize("hasRole('"+TOOLCRIBMANAGER+"')")
    public ResponseEntity<Page<ToolRequestItemDTO>> getAllToolRequestsForCribManager(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> fields,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime) {

        Page<ToolRequestItemDTO> requests = toolRequestService.getAllToolRequestsForCribManager(
                page, size, search, fields, startDateTime, endDateTime);

        return ResponseEntity.ok(requests);
    }



    @Operation(summary = "Approve/Reject Normal Tool Request", description = "Tool Crib Manager " +
            "approves or rejects a special tool request item.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request processed successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Tool request item not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request or already processed")
    })
    @PatchMapping("/tool-request-item/{Id}/decision")
    @PreAuthorize("hasRole('"+TOOLCRIBMANAGER+"')")
    public ResponseEntity<SuccessResponse> decideNormalRequest(
            @PathVariable Long Id,
            @RequestParam boolean approve) throws BadRequestException {

        return ResponseEntity.ok(toolRequestService.decideNormalToolRequest(Id, approve));
    }



    @Operation(summary = "Return Non-Perishable Tool",
            description = "Tool Crib Manager returns a non-perishable tool. Calculates fines if delayed or partially returned. Missing tools are marked as broken.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tool returned successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Tool request item not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request or already returned")
    })
    @PreAuthorize("hasRole('" + TOOLCRIBMANAGER + "')")
    @PutMapping("/returns")
    public ResponseEntity<SuccessResponse> returnTool
            (@RequestBody ToolReturnRequestDTO requestDTO) throws BadRequestException {
        return ResponseEntity.ok(toolRequestService.returnTool(requestDTO));
    }


    @GetMapping("/inventory")
    @PreAuthorize("hasRole('" + TOOLCRIBMANAGER + "')")
    public ResponseEntity<Page<ToolInventoryDTO>> getInventoryForToolCribManager(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) List<ToolCategory> category,
            @RequestParam(required = false) Boolean isPerishable,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
    ) {
        String email = authentication.getName();
        Page<ToolInventoryDTO> inventory = inventoryService.getInventoryForToolCribManager(
                email, name, category, isPerishable, minPrice, maxPrice, page, size
        );
        return ResponseEntity.ok(inventory);
    }


    @GetMapping("/unreturned-tools/workers")
    @PreAuthorize("hasRole('" + TOOLCRIBMANAGER + "')")
    public ResponseEntity<SuccessResponse> getWorkersWithUnreturnedTools() {
        return ResponseEntity.ok(toolRequestService.getUnreturnedToolWorkers());
    }

    @GetMapping("/worker/{id}")
    @PreAuthorize("hasRole('" + TOOLCRIBMANAGER + "')")
    public ResponseEntity<SuccessResponse> getSingleWorkerDetails(@PathVariable long id) {
        return ResponseEntity.ok(userService.getWorkerDetails(id));
    }

}

