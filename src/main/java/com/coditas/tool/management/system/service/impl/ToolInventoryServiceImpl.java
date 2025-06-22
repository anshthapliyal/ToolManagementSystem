package com.coditas.tool.management.system.service.impl;

import com.coditas.tool.management.system.dto.tool.AssignToolRequestDTO;
import com.coditas.tool.management.system.dto.sharedResponse.SuccessResponse;
import com.coditas.tool.management.system.dto.tool.ToolInventoryDTO;
import com.coditas.tool.management.system.entity.*;
import com.coditas.tool.management.system.exception.PremiseNotFoundException;
import com.coditas.tool.management.system.exception.UserNotFoundException;
import com.coditas.tool.management.system.repository.*;
import com.coditas.tool.management.system.service.ToolInventoryService;
import com.coditas.tool.management.system.specification.ToolInventorySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ToolInventoryServiceImpl implements ToolInventoryService {

    private final WorkplaceRepository workplaceRepository;
    private final ToolCribRepository toolCribRepository;
    private final ToolRepository toolRepository;
    private final ToolInventoryRepository toolInventoryRepository;
    private final UserRepository userRepository;
    private final ToolInventoryLogRepository logRepository;

    public ToolInventoryServiceImpl
            (WorkplaceRepository workplaceRepository, ToolCribRepository toolCribRepository,
             ToolRepository toolRepository, ToolInventoryRepository toolInventoryRepository,
             UserRepository userRepository, ToolInventoryLogRepository logRepository) {
        this.workplaceRepository = workplaceRepository;
        this.toolCribRepository = toolCribRepository;
        this.toolRepository = toolRepository;
        this.toolInventoryRepository = toolInventoryRepository;
        this.userRepository = userRepository;
        this.logRepository = logRepository;
    }

    @Override
    public SuccessResponse assignToolToWorkplace(AssignToolRequestDTO request) {
        //Get workplace
        Workplace workplace = workplaceRepository.findById(request.getWorkplaceId())
                .orElseThrow(() -> new PremiseNotFoundException("Workplace not found"));

        //Jurisdiction of current logged in user
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).get();
        if (!workplace.getFacility().getFacilityManager().getId().equals(user.getId())) {
            throw new AccessDeniedException("You can only manage your own workplace.");
        }

        //Get the tool crib associated with workplace
        ToolCrib toolCrib = toolCribRepository.findByWorkplaceId(workplace.getId())
                .orElseThrow(() -> new PremiseNotFoundException("ToolCrib not found for workplace"));

        //Get the tool
        Tool tool = toolRepository.findById(request.getToolId())
                .orElseThrow(() -> new PremiseNotFoundException("Tool not found"));

        //Check if tool already assigned to that ToolCrib
        Optional<ToolInventory> optionalInventory = toolInventoryRepository
                .findByToolCribAndTool(toolCrib, tool);

        if (optionalInventory.isPresent()) {
            ToolInventory inventory = optionalInventory.get();
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() + request.getQuantity());
            inventory.setTotalQuantity(inventory.getTotalQuantity() + request.getQuantity());
            toolInventoryRepository.save(inventory);
        } else {
            ToolInventory inventory = ToolInventory.builder()
                    .tool(tool)
                    .toolCrib(toolCrib)
                    .availableQuantity(request.getQuantity())
                    .totalQuantity(request.getQuantity())
                    .minimumThreshold(0L) // default
                    .build();
            toolInventoryRepository.save(inventory);
        }

        ToolInventoryLog log = ToolInventoryLog.builder()
                .tool(tool)
                .toolCrib(toolCrib)
                .workplace(workplace)
                .assignedBy(user)
                .quantityAssigned(request.getQuantity())
                .build();
        logRepository.save(log);
        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Inventory updated successfully.")
                .time(LocalDateTime.now())
                .build();
    }

    @Override
    public Page<ToolInventoryDTO> getToolInventoryForToolCrib(Long toolCribId, int page,
                                                              int size, boolean filterLowStock) {

        String managerEmail = SecurityContextHolder.getContext().getAuthentication().getName();

         User facManager = userRepository.findByEmail(managerEmail)
                 .orElseThrow(() -> new UserNotFoundException("Authenticated Facility Manager Not Found."));

        Pageable pageable = PageRequest.of(page, size, Sort.by("tool.name").ascending());

        Specification<ToolInventory> spec = Specification.where(ToolInventorySpecification.belongsToToolCrib(toolCribId))
                .and(ToolInventorySpecification.belongsToManagerEmail(managerEmail));

        if (filterLowStock) {
            spec = spec.and(ToolInventorySpecification.availableLessThanMinimumThreshold());
        }

        Page<ToolInventory> resultPage = toolInventoryRepository.findAll(spec, pageable);

        // Mapping each ToolInventory entity to ToolInventoryDTO
        return resultPage.map(entity -> ToolInventoryDTO.builder()
                .toolId(entity.getTool().getId())
                .toolName(entity.getTool().getName())
                .totalQuantity(entity.getTotalQuantity())
                .availableQuantity(entity.getAvailableQuantity())
                .toolCribId(entity.getToolCrib().getId())
                .toolCribName(entity.getToolCrib().getName())
                .minimumThreshold(entity.getMinimumThreshold())
                .toolImageUrl(entity.getTool().getToolImageUrl())
                .fineAmount(entity.getTool().getFineAmount())
                .brokenQuantity(entity.getBrokenQuantity())
                .returnPeriod(entity.getTool().getReturnPeriod())
                .toolCategory(entity.getTool().getCategory())
                .isPerishable(entity.getTool().getIsPerishable())
                .build());
    }


}
