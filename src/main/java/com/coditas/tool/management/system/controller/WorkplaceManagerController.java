package com.coditas.tool.management.system.controller;

import com.coditas.tool.management.system.constant.ToolCategory;
import com.coditas.tool.management.system.dto.premises.WorkstationDTO;
import com.coditas.tool.management.system.dto.sharedResponse.SuccessResponse;
import com.coditas.tool.management.system.dto.tool.ToolInventoryDTO;
import com.coditas.tool.management.system.dto.tool.ToolRequestItemDTO;
import com.coditas.tool.management.system.dto.user.MgrUpdateReqDTO;
import com.coditas.tool.management.system.dto.user.UserDTO;
import com.coditas.tool.management.system.dto.user.UserListDTO;
import com.coditas.tool.management.system.dto.user.WorkerListDTO;
import com.coditas.tool.management.system.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
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

import static com.coditas.tool.management.system.constant.AuthorityConstant.*;

@RestController
@RequestMapping("/workplace-manager")
@Validated
public class WorkplaceManagerController {

    private final UserService userService;
    private final WorkstationService workstationService;
    private final ToolCribService toolCribService;
    private final ToolRequestService toolRequestService;
    private final InventoryService inventoryService;


    @Autowired
    public WorkplaceManagerController(UserService userService, WorkstationService workstationService,
                                      ToolCribService toolCribService, ToolRequestService toolRequestService, InventoryService inventoryService) {
        this.userService = userService;
        this.workstationService = workstationService;
        this.toolCribService = toolCribService;
        this.toolRequestService = toolRequestService;
        this.inventoryService = inventoryService;
    }

    @Operation(summary = "Add Worker", description = "End point to add Worker.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Worker Manager added successfully!"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Validation failed")
    })
    @PostMapping("/worker")
    @PreAuthorize("hasAnyRole('"+OWNER+"','"+FACILITYMANAGER+"','"+WORKPLACEMANAGER+"')")
    public ResponseEntity<SuccessResponse> addWorker(@Valid @RequestBody UserDTO dto) {
        return ResponseEntity.ok(userService.createWorker(dto));
    }


    @Operation(summary = "Update Worker", description = "End point to update Worker")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Worker Manager updated successfully!"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Validation failed")
    })
    @PatchMapping("/worker/{id}")
    @PreAuthorize("hasAnyRole('"+OWNER+"','"+FACILITYMANAGER+"','"+WORKPLACEMANAGER+"')")
    public ResponseEntity<SuccessResponse> updateWorker(@PathVariable long id,
                                                                 @Valid @RequestBody MgrUpdateReqDTO req) {
        return ResponseEntity.ok(userService.updateWorker(id, req));
    }


    @Operation(summary = "Delete Worker", description = "End point to delete Worker")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Worker deleted successfully!"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "400", description = "Worker Not Found"),
            @ApiResponse(responseCode = "500", description = "Validation failed")
    })
    @DeleteMapping("/worker/{id}")
    @PreAuthorize("hasAnyRole('"+OWNER+"','"+FACILITYMANAGER+"','"+WORKPLACEMANAGER+"')")
    public ResponseEntity<SuccessResponse> deleteWorker(@PathVariable long id) {
        return ResponseEntity.ok(userService.deleteWorker(id));
    }


    @Operation(
            summary = "View List of Workers",
            description = "Retrieve a paginated list of Workers with optional keyword-based filtering " +
                    "on selected fields (name, email, workplaceName, facilityName)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of Workers fetched successfully"),
            @ApiResponse(responseCode = "403", description =
                    "Forbidden - User does not have OWNER or Facility Manager or Workplace Manager role"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("/worker")
    @PreAuthorize("hasAnyRole('"+OWNER+"','"+FACILITYMANAGER+"','"+WORKPLACEMANAGER+"')")
    public ResponseEntity<Page<WorkerListDTO>> getAllWorkers(
            @Parameter(description = "Page number (zero-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of records per page", example = "5")
            @RequestParam(defaultValue = "5") int size,

            @Parameter(description = "Search keyword to match across selected fields", example = "Ansh")
            @RequestParam(required = false) String search,

            @Parameter(
                    description = "Fields to apply the search on. " +
                            "Possible values: name, email, workplaceName, facilityName.",
                    examples = {
                            @ExampleObject(name = "Search by name", value = "[\"name\"]"),
                            @ExampleObject(name = "Search by email", value = "[\"email\"]"),
                            @ExampleObject(name = "Search by all",
                                    value = "[\"name\", \"email\", \"workplaceName\", \"facilityName\"]")
                    }
            )
            @RequestParam(required = false) List<String> fields
    ) {
        return ResponseEntity.ok(userService.getWorkers(page, size, search, fields));
    }


    @Operation(
            summary = "Get Available Workers (Unassigned) (For Dropdown)",
            description = "Returns a list of Worker emails not assigned to any workstation. " +
                    "Supports partial search by name or email."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of available Worker emails"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/worker/available")
    @PreAuthorize("hasAnyRole('"+OWNER+"','"+FACILITYMANAGER+"','"+WORKPLACEMANAGER+"')")
    public ResponseEntity<SuccessResponse> getAvailableWorkers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> fields
    ) {
        return ResponseEntity.ok(userService.getAvailableWorkers(search, fields));
    }

    //Workstation

    @Operation(summary = "Add workstation", description = "End point to add workstation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workstation added successfully!"),
            @ApiResponse(responseCode = "400", description = "Workplace Manager/Worker Not Found"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Validation failed")
    })
    @PostMapping("/workstation")
    @PreAuthorize("hasAnyRole('"+OWNER+"','"+FACILITYMANAGER+"','"+WORKPLACEMANAGER+"')")
    public ResponseEntity<SuccessResponse> createWorkplace(@Valid @RequestBody WorkstationDTO workstationDTO) {
        return ResponseEntity.ok(workstationService.createWorkstation(workstationDTO));
    }


    @Operation(summary = "Update workstation", description = "End point to update workstation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workstation updated successfully!"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Validation failed")
    })
    @PatchMapping("/workstation/{id}")
    @PreAuthorize("hasAnyRole('"+OWNER+"','"+FACILITYMANAGER+"','"+WORKPLACEMANAGER+"')")
    public ResponseEntity<SuccessResponse> updateWorkstation(@PathVariable long id,
                                                           @Valid @RequestBody WorkstationDTO workstationDTO){
        return ResponseEntity.ok(workstationService.updateWorkstation(id ,workstationDTO));
    }


    @Operation(summary = "Delete workstation", description = "End point to delete workstation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workstation deleted successfully!"),
            @ApiResponse(responseCode = "400", description = "Workstation Not Found"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Validation failed")
    })
    @DeleteMapping("/workstation/{id}")
    @PreAuthorize("hasAnyRole('"+OWNER+"','"+FACILITYMANAGER+"','"+WORKPLACEMANAGER+"')")
    public ResponseEntity<SuccessResponse> deleteWorkstation(@PathVariable long id){
        return ResponseEntity.ok(workstationService.deleteWorkstation(id));
    }


    @Operation(
            summary = "View list of Workstations under the specific Workplace Manager",
            description =
                    "Endpoint to view all workstations assigned to workplaces " +
                            "managed by the logged-in Workplace Manager"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of Workstations retrieved successfully."),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("/workstation")
    @PreAuthorize("hasAnyRole('" + OWNER + "','" + FACILITYMANAGER + "','" + WORKPLACEMANAGER + "')")
    public ResponseEntity<Page<WorkstationDTO>> getAllWorkstations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> fields,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(workstationService.findAllWorkstations(page, size, email, search, fields));
    }




    @Operation(
            summary = "Create Tool Crib Manager",
            description = "API to create a Tool Crib Manager and link them to the tool crib of the logged-in Workplace Manager's workplace"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description =
                    "Tool Crib Manager created and linked successfully."),
            @ApiResponse(responseCode = "400", description =
                    "Invalid request or email already exists"),
            @ApiResponse(responseCode = "404", description
                    = "Workplace or Tool Crib not found for logged-in manager"),
            @ApiResponse(responseCode = "500", description =
                    "Internal server error")
    })
    @PostMapping("/tool-crib-manager")
    @PreAuthorize("hasAnyRole('"+OWNER+"','"+FACILITYMANAGER+"','"+WORKPLACEMANAGER+"')")
    public ResponseEntity<SuccessResponse> createToolCribManager(@RequestBody UserDTO dto) {
        return ResponseEntity.ok(toolCribService.createToolCribManager(dto));
    }


    @PatchMapping("/tool-crib-manager/{id}")
    @PreAuthorize("hasAnyRole('" + OWNER + "','" + FACILITYMANAGER + "','" + WORKPLACEMANAGER + "')")
    public ResponseEntity<SuccessResponse> updateToolCribManager(@PathVariable Long id, @RequestBody UserDTO dto) throws BadRequestException {
        return ResponseEntity.ok(toolCribService.updateToolCribManager(id, dto));
    }


    @DeleteMapping("/tool-crib-manager/{id}")
    @PreAuthorize("hasAnyRole('" + OWNER + "','" + FACILITYMANAGER + "','" + WORKPLACEMANAGER + "')")
    public ResponseEntity<SuccessResponse> deleteToolCribManager(@PathVariable Long id) throws BadRequestException {
        return ResponseEntity.ok(toolCribService.deleteToolCribManager(id));
    }


    @GetMapping("/tool-crib-manager")
    @PreAuthorize("hasAnyRole('" + OWNER + "','" + FACILITYMANAGER + "','" + WORKPLACEMANAGER + "')")
    public ResponseEntity<Page<UserListDTO>> getToolCribManagers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> fields) {
        return ResponseEntity.ok(toolCribService.getToolCribManagers(page, size, search, fields));
    }

    // /tool-crib-managers?page=0&size=5&search=john&fields=name,email




    @Operation(
            summary = "Get special tool requests for a Workplace Manager",
            description = "Returns a paginated list of tool requests marked as SPECIAL that need approval or are " +
                    "already approved by the authenticated Workplace Manager. Supports filtering by tool name, " +
                    "worker name, and request date range."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Access denied")
    })
    @GetMapping("/special-tool-requests")
    @PreAuthorize("hasAnyRole('" + OWNER + "','" + FACILITYMANAGER + "','" + WORKPLACEMANAGER + "')")
    public ResponseEntity<Page<ToolRequestItemDTO>> getSpecialToolRequestsForManager(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> fields,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime
    ) {
        Page<ToolRequestItemDTO> result = toolRequestService.getSpecialRequestsForManager(
                page, size, search, fields, startDateTime, endDateTime
        );
        return ResponseEntity.ok(result);
    }



    @Operation(summary = "Approve/Reject Special Tool Request", description = "Workplace Manager approves or rejects a special tool request item.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request processed successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Tool request item not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request or already processed")
    })
    @PatchMapping("/tool-request-item/{id}/decision")
    @PreAuthorize("hasAnyRole('"+OWNER+"','"+FACILITYMANAGER+"','"+WORKPLACEMANAGER+"')")
    public ResponseEntity<SuccessResponse> decideSpecialRequest(
            @PathVariable Long id,
            @RequestParam("approve") boolean approve) throws BadRequestException {

        return ResponseEntity.ok(toolRequestService.decideSpecialRequest(id, approve));
    }

    @GetMapping("/inventory")
    @PreAuthorize("hasAnyRole('" + OWNER + "','" + FACILITYMANAGER + "','" + WORKPLACEMANAGER + "')")
    public ResponseEntity<Page<ToolInventoryDTO>> viewInventory(
            Authentication authentication,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,

            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean isPerishable,
            @RequestParam(required = false) List<ToolCategory> category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
    ) {
        String email = authentication.getName();
        Page<ToolInventoryDTO> inventoryPage = inventoryService.getInventoryForManager(
                email, name, isPerishable, category, minPrice, maxPrice, page, size
        );
        return ResponseEntity.ok(inventoryPage);
    }
}

