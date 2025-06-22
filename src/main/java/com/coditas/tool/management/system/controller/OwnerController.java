package com.coditas.tool.management.system.controller;
import com.coditas.tool.management.system.constant.ToolCategory;
import com.coditas.tool.management.system.dto.premises.FacilityDTO;
import com.coditas.tool.management.system.dto.premises.FacilityListDTO;
import com.coditas.tool.management.system.dto.premises.WorkplaceListDTO;
import com.coditas.tool.management.system.dto.sharedResponse.SuccessResponse;
import com.coditas.tool.management.system.dto.tool.ToolDTO;
import com.coditas.tool.management.system.dto.user.MgrUpdateReqDTO;
import com.coditas.tool.management.system.dto.user.UserDTO;
import com.coditas.tool.management.system.dto.user.UserHierarchyDTO;
import com.coditas.tool.management.system.dto.user.UserListDTO;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.coditas.tool.management.system.constant.AuthorityConstant.OWNER;

@RestController
@RequestMapping("/owner")
@Validated
public class OwnerController {

    private final UserService userService;
    private final FacilityService facilityService;
    private final ToolService toolService;
    private final ReportService reportService;
    private final WorkplaceService workplaceService;


    @Autowired
    public OwnerController(UserService userService, FacilityService facilityService,
                           ToolService toolService, ReportService reportService, WorkplaceService workplaceService) {
        this.userService = userService;
        this.facilityService = facilityService;
        this.toolService = toolService;
        this.reportService = reportService;
        this.workplaceService = workplaceService;
    }

    //Facility Manager

    @Operation(summary = "Add facility manager", description = "End point to add facility manager")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Facility Manager added successfully!"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Validation failed")
    })
    @PostMapping("/facility-manager")
    @PreAuthorize("hasRole('"+OWNER+"')")
    public ResponseEntity<SuccessResponse> addFacilityManager(@Valid @RequestBody UserDTO dto) {
        return ResponseEntity.ok(userService.createFacilityManager(dto));
    }


    @Operation(summary = "Update facility manager", description = "End point to update facility manager")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Facility Manager updated successfully!"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Validation failed")
    })
    @PatchMapping("/facility-manager/{id}")
    @PreAuthorize("hasRole('"+OWNER+"')")
    public ResponseEntity<SuccessResponse> updateFacilityManager(@PathVariable long id, @Valid @RequestBody MgrUpdateReqDTO req) {
        return ResponseEntity.ok(userService.updateFacilityManager(id, req));
    }


    @Operation(summary = "Delete facility manager", description = "End point to delete facility manager")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Facility Manager deleted successfully!"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "400", description = "Facility Manager Not Found"),
            @ApiResponse(responseCode = "500", description = "Validation failed")
    })
    @DeleteMapping("/facility-manager/{id}")
    @PreAuthorize("hasRole('"+OWNER+"')")
    public ResponseEntity<SuccessResponse> deleteFacilityManager(@PathVariable long id) {
        return ResponseEntity.ok(userService.deleteFacilityManager(id));
    }


    @Operation(
            summary = "View List of Facility Managers",
            description = "Retrieve a paginated list of Facility Managers " +
                    "with optional keyword-based filtering on selected fields (name, email)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of Facility Managers fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have OWNER role"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("/facility-manager")
    @PreAuthorize("hasRole('" + OWNER + "')")
    public ResponseEntity<Page<UserListDTO>> getAllFacilityManagers(
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
        return ResponseEntity.ok(userService.getFacilityManagers(page, size, search, fields));
    }


    @Operation(
            summary = "Get Available Facility Managers (Unassigned) (For Dropdown)",
            description = "Returns a list of facility manager emails not assigned to any facility. Supports partial search by name or email."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of available Facility Manager emails"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/facility-manager/available")
    @PreAuthorize("hasRole('"+OWNER+"')")
    public ResponseEntity<SuccessResponse> getAvailableFacilityManagers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> fields
    ) {
        return ResponseEntity.ok(userService.getAvailableFacilityManagers(search, fields));
    }



    //Facility

    @Operation(summary = "Add facility", description = "End point to add facility")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Facility added successfully!"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Validation failed")
    })
    @PostMapping("/facility")
    @PreAuthorize("hasRole('"+OWNER+"')")
    public ResponseEntity<SuccessResponse> createFacility(@Valid @RequestBody FacilityDTO facilityDto) {
        return ResponseEntity.ok(facilityService.createFacility(facilityDto));
    }

    @Operation(summary = "Update facility", description = "End point to update facility")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Facility updated successfully!"),
            @ApiResponse(responseCode = "400", description = "Facility Not Found"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Validation failed")
    })
    @PatchMapping("/facility/{id}")
    @PreAuthorize("hasRole('"+OWNER+"')")
    public ResponseEntity<SuccessResponse> updateFacility(@PathVariable long id, @Valid @RequestBody FacilityDTO facilityDTO){
        return ResponseEntity.ok(facilityService.updateFacility(id ,facilityDTO));
    }

    @Operation(summary = "Delete facility", description = "End point to delete facility")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Facility deleted successfully!"),
            @ApiResponse(responseCode = "400", description = "Facility Not Found"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Validation failed")
    })
    @DeleteMapping("/facility/{id}")
    @PreAuthorize("hasRole('"+OWNER+"')")
    public ResponseEntity<SuccessResponse> deleteFacility(@PathVariable long id){
        return ResponseEntity.ok(facilityService.deleteFacility(id));
    }


    @Operation(
            summary = "View List of Facilities",
            description = "Endpoint to view all active facilities with optional filtering and pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of Facilities"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Validation failed")
    })
    @GetMapping("/facility")
    @PreAuthorize("hasRole('"+OWNER+"')")
    public ResponseEntity<Page<FacilityListDTO>> getAllFacilities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> fields
    ) {
        return ResponseEntity.ok(facilityService.findAllFacilities(page, size, search, fields));
    }

    //Tool Service

    @Operation(summary = "Add new Tool", description = "End point to add a single tool with optional image upload.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Added tool"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Validation failed")
    })
    @PostMapping(value = "/tool", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('" + OWNER + "')")
    public ResponseEntity<SuccessResponse> addTools(
            @ModelAttribute ToolDTO toolDTO,
            @RequestPart(value = "toolImage", required = false) MultipartFile toolImage) {
        return ResponseEntity.ok(toolService.addTools(toolDTO, toolImage));
    }




    @Operation(summary = "Get all Tools", description = "Endpoint to fetch paginated, filtered list of tools")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched tools list"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Validation failed")
    })
    @GetMapping("/tool")
    @PreAuthorize("hasRole('" + OWNER + "')")
    public ResponseEntity<Page<ToolDTO>> getAllTools(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean isPerishable,
            @RequestParam(required = false) List<ToolCategory> category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<ToolDTO> tools = toolService.getTools(name, isPerishable, category, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(tools);
    }


    @Operation(summary = "Update a Tool", description = "Endpoint to update a tool with optional image upload.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated tool"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "400", description = "Tool Not Found"),
            @ApiResponse(responseCode = "500", description = "Validation failed")
    })
    @PatchMapping(value = "/tool/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('" + OWNER + "')")
    public ResponseEntity<SuccessResponse> updateTools(
            @PathVariable long id,
            @ModelAttribute ToolDTO toolDTO,
            @RequestPart(value = "toolImage", required = false) MultipartFile toolImage) {

        return ResponseEntity.ok(toolService.updateTools(id, toolDTO, toolImage));
    }


    @Operation(summary = "Delete a Tool", description = "End point to delete a single tool.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deleted tool"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "400", description = "Tool Not Found"),
            @ApiResponse(responseCode = "500", description = "Validation failed")
    })
    @DeleteMapping("/tool/{id}")
    @PreAuthorize("hasRole('"+OWNER+"')")
    public ResponseEntity<SuccessResponse> deleteTools(@PathVariable long id){
        return ResponseEntity.ok(toolService.deleteTools(id));
    }


    //Reports
    @GetMapping("/top-demanded-tools")
    @PreAuthorize("hasRole('"+OWNER+"')")
    public ResponseEntity<SuccessResponse> getTopDemandedTools() {
        return ResponseEntity.ok(reportService.getTopDemandedTools());
    }


    @GetMapping("/most-broken-tools")
    @PreAuthorize("hasRole('" + OWNER + "')")
    public ResponseEntity<SuccessResponse> getMostBrokenTools() {
        return ResponseEntity.ok(reportService.getMostBrokenTools());
    }


    @GetMapping("/top-priced-tools")
    @PreAuthorize("hasRole('" + OWNER + "')")
    public ResponseEntity<SuccessResponse> getTopPricedTools() {
        return ResponseEntity.ok(reportService.getTopPricedTools());
    }

    //View Whole Organization
    @GetMapping("/employees")
    @PreAuthorize("hasRole('" + OWNER + "')")
    public ResponseEntity<Page<UserHierarchyDTO>> getAllUserHierarchies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String email
    ) {
        return ResponseEntity.ok(userService.getAllUserHierarchies(page, size, role, email));
    }


    @GetMapping("/facility/{facilityId}/workplaces")
    @PreAuthorize("hasRole('" + OWNER + "')")
    public ResponseEntity<Page<WorkplaceListDTO>> getWorkplacesByFacility(
            @PathVariable Long facilityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> fields
    ) {
        return ResponseEntity.ok(workplaceService.getWorkplacesByFacility(facilityId, page, size, search, fields));
    }
}
