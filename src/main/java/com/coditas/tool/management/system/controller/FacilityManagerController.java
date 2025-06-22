package com.coditas.tool.management.system.controller;

import com.coditas.tool.management.system.constant.ToolCategory;
import com.coditas.tool.management.system.dto.premises.WorkplaceDTO;
import com.coditas.tool.management.system.dto.premises.WorkplaceListDTO;
import com.coditas.tool.management.system.dto.sharedResponse.SuccessResponse;
import com.coditas.tool.management.system.dto.tool.*;
import com.coditas.tool.management.system.dto.user.MgrUpdateReqDTO;
import com.coditas.tool.management.system.dto.user.UserDTO;
import com.coditas.tool.management.system.dto.user.WrkMngrListDTO;
import com.coditas.tool.management.system.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static com.coditas.tool.management.system.constant.AuthorityConstant.FACILITYMANAGER;
import static com.coditas.tool.management.system.constant.AuthorityConstant.OWNER;

@RestController
@RequestMapping("/facility-manager")
@Validated
public class FacilityManagerController {

    private final UserService userService;
    private final WorkplaceService workplaceService;
    private final ToolInventoryService toolInventoryService;
    private final ToolInventoryLogService toolInventoryLogService;
    private final ToolService toolService;
    private final ToolCribService toolCribService;

    @Autowired
    public FacilityManagerController(UserService userService, WorkplaceService workplaceService, ToolInventoryService toolInventoryService, ToolInventoryLogService toolInventoryLogService, ToolService toolService, ToolCribService toolCribService) {
        this.userService = userService;
        this.workplaceService = workplaceService;
        this.toolInventoryService = toolInventoryService;
        this.toolInventoryLogService = toolInventoryLogService;
        this.toolService = toolService;
        this.toolCribService = toolCribService;
    }

    //Workplace Manager

    @Operation(summary = "Add workplace manager", description = "End point to add workplace manager.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workplace Manager added successfully!"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Validation failed")
    })
    @PostMapping("/workplace-manager")
    @PreAuthorize("hasAnyRole('"+OWNER+"','"+FACILITYMANAGER+"')")
    public ResponseEntity<SuccessResponse> addWorkplaceManager(@Valid @RequestBody UserDTO dto) {
        return ResponseEntity.ok(userService.createWorkplaceManager(dto));
    }

    @Operation(summary = "Update workplace manager", description = "End point to update workplace manager")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workplace Manager updated successfully!"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Validation failed")
    })
    @PatchMapping("/workplace-manager/{id}")
    @PreAuthorize("hasAnyRole('"+OWNER+"','"+FACILITYMANAGER+"')")
    public ResponseEntity<SuccessResponse> updateFacilityManager(@PathVariable long id, @Valid @RequestBody MgrUpdateReqDTO req) {
        return ResponseEntity.ok(userService.updateWorkplaceManager(id, req));
    }


    @Operation(summary = "Delete workplace manager", description = "End point to delete worlplace manager")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workplace Manager deleted successfully!"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "400", description = "Workplace Manager Not Found"),
            @ApiResponse(responseCode = "500", description = "Validation failed")
    })
    @DeleteMapping("/workplace-manager/{id}")
    @PreAuthorize("hasAnyRole('"+OWNER+"','"+FACILITYMANAGER+"')")
    public ResponseEntity<SuccessResponse> deleteWorkplaceManager(@PathVariable long id) {
        return ResponseEntity.ok(userService.deleteWorkplaceManager(id));
    }


    @Operation(
            summary = "View List of Workplace Managers",
            description = "Retrieve a paginated list of Workplace Managers " +
                    "with optional keyword-based filtering on selected fields (name, email)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of Workplace Managers fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have OWNER/Facility " +
                    "Manager role"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("/workplace-manager")
    @PreAuthorize("hasAnyRole('"+OWNER+"','"+FACILITYMANAGER+"')")
    public ResponseEntity<Page<WrkMngrListDTO>> getAllWorkplaceManagers(
            @Parameter(description = "Page number (zero-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of records per page", example = "5")
            @RequestParam(defaultValue = "5") int size,

            @Parameter(description = "Search keyword to match across selected fields", example = "ansh")
            @RequestParam(required = false) String search,

            @Parameter(
                    description = "Fields to apply the search on. Possible values: name, email.",
                    examples = {
                            @ExampleObject(name = "Search by email", value = "[\"email\"]"),
                            @ExampleObject(name = "Search by name", value = "[\"name\"]"),
                            @ExampleObject(name = "Search by all fields", value = "[\"name\", \"email\"]")
                    }
            )
            @RequestParam(required = false) List<String> fields
    ) {
        return ResponseEntity.ok(userService.getWorkplaceManagers(page, size, search, fields));
    }

    @Operation(
            summary = "Get Available Workplace Managers (Unassigned) (For Dropdown)",
            description = "Returns a list of Workplace manager emails not assigned to any facility. " +
                    "Supports partial search by name or email."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of available Workplace Manager emails"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/workplace-manager/available")
    @PreAuthorize("hasAnyRole('"+OWNER+"','"+FACILITYMANAGER+"')")
    public ResponseEntity<SuccessResponse> getAvailableWorkplaceManagers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> fields
    ) {
        return ResponseEntity.ok(userService.getAvailableWorkplaceManagers(search, fields));
    }



    //Workplace

    @Operation(summary = "Add workplace", description = "End point to add workplace")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workplace added successfully!"),
            @ApiResponse(responseCode = "400", description = "Facility/Workplace Manager Not Found"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Validation failed")
    })
    @PostMapping("/workplace")
    @PreAuthorize("hasAnyRole('"+OWNER+"','"+FACILITYMANAGER+"')")
    public ResponseEntity<SuccessResponse> createWorkplace(@Valid @RequestBody WorkplaceDTO workplaceDTO) {
        return ResponseEntity.ok(workplaceService.createWorkplace(workplaceDTO));
    }


    @Operation(summary = "Update workplace", description = "End point to update workplace")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workplace updated successfully!"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Validation failed")
    })
    @PatchMapping("/workplace/{id}")
    @PreAuthorize("hasAnyRole('"+OWNER+"','"+FACILITYMANAGER+"')")
    public ResponseEntity<SuccessResponse> updateWorkplace(@PathVariable long id,
                                                           @Valid @RequestBody WorkplaceDTO workplaceDTO){
        return ResponseEntity.ok(workplaceService.updateWorkplace(id ,workplaceDTO));
    }


    @Operation(summary = "Delete workplace", description = "End point to delete workplace")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "workplace deleted successfully!"),
            @ApiResponse(responseCode = "400", description = "workplace Not Found"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Validation failed")
    })
    @DeleteMapping("/workplace/{id}")
    @PreAuthorize("hasAnyRole('"+OWNER+"','"+FACILITYMANAGER+"')")
    public ResponseEntity<SuccessResponse> deleteWorkplace(@PathVariable long id){
        return ResponseEntity.ok(workplaceService.deleteWorkplace(id));
    }


    @Operation(
            summary = "View List of Workplaces under the specific facility manager",
            description = "Endpoint to view all workplaces with optional filtering and pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of Workplaces under the specific facility."),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Validation failed")
    })
    @GetMapping("/workplace")
    @PreAuthorize("hasAnyRole('" + OWNER + "','" + FACILITYMANAGER + "')")
    public ResponseEntity<Page<WorkplaceListDTO>> getAllWorkplaces(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> fields,
            Authentication authentication
    ) {
        String email = authentication.getName(); // email of logged in facility manager
        return ResponseEntity.ok(workplaceService.findAllWorkplaces(page, size, search, fields, email));
    }


    @Operation(
            summary = "Fetch paginated Tool Cribs with selective field search",
            description = "Returns paginated Tool Crib data where `search` is applied to selected `fields` with AND logic. Fields can include name, workplaceName, and toolCribManagerEmail."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tool Cribs fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only Facility Manager access allowed"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("/tool-cribs")
    @PreAuthorize("hasAnyRole('" + OWNER + "','" + FACILITYMANAGER + "')")
    public Page<ToolCribDetailsDto> getToolCribsForManager(

            @Parameter(description = "Search keyword applied to selected fields", example = "ham")
            @RequestParam(required = false) String search,

            @Parameter(description = "Fields to apply the search on (e.g. name, workplaceName, toolCribManagerEmail)"
                    , example = "[name, workplaceName]")
            @RequestParam(required = false) List<String> fields,

            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "5") int size
    ) {
        return toolCribService.getToolCribsForFacilityManager(search, fields, page, size);
    }
    //facility-manager/tool-cribs


    @GetMapping("/tool-cribs/{toolCribId}/inventory")
    @PreAuthorize("hasAnyRole('" + OWNER + "','" + FACILITYMANAGER + "')")
    public ResponseEntity<Page<ToolInventoryDTO>> getToolInventoryOfToolCrib(
            @PathVariable Long toolCribId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "false") boolean filterLowStock
    )
    {
        Page<ToolInventoryDTO> inventory = toolInventoryService.getToolInventoryForToolCrib(toolCribId,
                page, size, filterLowStock);
        return ResponseEntity.ok(inventory);
    }














    //List of tools -> same api owner/tools
    @Operation(summary = "Get all Tools", description = "Endpoint to fetch paginated, filtered list of tools")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched tools list"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Validation failed")
    })
    @GetMapping("/tool")
    @PreAuthorize("hasAnyRole('" + OWNER + "','" + FACILITYMANAGER + "')")
    public ResponseEntity<Page<ToolDTO>> getAllTools(
            @Parameter(description = "Page number (zero-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of records per page", example = "5")
            @RequestParam(defaultValue = "5") int size,

            @Parameter(description = "Partial name match (case-insensitive)", example = "drill")
            @RequestParam(required = false) String name,

            @Parameter(description = "Whether the tool is perishable", example = "true")
            @RequestParam(required = false) Boolean isPerishable,

            @Parameter(description = "Category of the tool", example = "SPECIAL")
            @RequestParam(required = false) List<ToolCategory> category,

            @Parameter(description = "Minimum price", example = "100")
            @RequestParam(required = false) Double minPrice,

            @Parameter(description = "Maximum price", example = "1000")
            @RequestParam(required = false) Double maxPrice
    )
    {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<ToolDTO> tools = toolService.getTools(name, isPerishable, category, minPrice, maxPrice, pageable);

        return ResponseEntity.ok(tools);
    }

    @PostMapping("/tools")
    @PreAuthorize("hasAnyRole('" + OWNER + "','" + FACILITYMANAGER + "')")
    public ResponseEntity<SuccessResponse> assignToolToWorkplace(@RequestBody AssignToolRequestDTO request) {
        return ResponseEntity.ok(toolInventoryService.assignToolToWorkplace(request));
    }

    //Logging API
    @GetMapping("/tool-logs")
    @PreAuthorize("hasAnyRole('" + OWNER + "','" + FACILITYMANAGER + "')")
    public ResponseEntity<Page<ToolInventoryLogDTO>> getAllToolInventoryLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime minDateTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime maxDateTime
    ) {
        return ResponseEntity.ok(toolInventoryLogService.getLogs(page, size, minDateTime, maxDateTime));
    }

}
