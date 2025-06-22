package com.coditas.tool.management.system.service.impl;

import com.coditas.tool.management.system.constant.ToolCategory;
import com.coditas.tool.management.system.dto.tool.ToolInventoryDTO;
import com.coditas.tool.management.system.entity.*;
import com.coditas.tool.management.system.exception.ResourceNotFoundException;
import com.coditas.tool.management.system.exception.UserNotFoundException;
import com.coditas.tool.management.system.repository.*;
import com.coditas.tool.management.system.service.InventoryService;
import com.coditas.tool.management.system.specification.ToolInventorySpecification;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService{

    private final UserRepository userRepository;
    private final ToolInventoryRepository toolInventoryRepository;
    private final WorkplaceRepository workplaceRepository;
    private final ToolCribRepository toolCribRepository;
    private final WorkstationRepository workstationRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public InventoryServiceImpl
            (UserRepository userRepository, ToolInventoryRepository toolInventoryRepository,
             WorkplaceRepository workplaceRepository, ToolCribRepository toolCribRepository,
             WorkstationRepository workstationRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.toolInventoryRepository = toolInventoryRepository;
        this.workplaceRepository = workplaceRepository;
        this.toolCribRepository = toolCribRepository;
        this.workstationRepository = workstationRepository;
        this.modelMapper = modelMapper;
    }

    //Workplace manager can view inventory of own tool crib
    public Page<ToolInventoryDTO> getInventoryForManager(String email, String name, Boolean isPerishable,
                                                         List<ToolCategory> category, Double minPrice,
                                                         Double maxPrice, int page, int size) {

        User manager = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Workplace workplace = workplaceRepository.findByWorkplaceManager(manager)
                .orElseThrow(() -> new UserNotFoundException("No workplace assigned to manager"));

        ToolCrib toolCrib = toolCribRepository.findByWorkplace(workplace)
                .orElseThrow(() -> new UserNotFoundException("No ToolCrib found for workplace"));

        Pageable pageable = PageRequest.of(page, size);

        Specification<ToolInventory> spec = ToolInventorySpecification.filterInventory(
                toolCrib.getId(), name, isPerishable, category, minPrice, maxPrice
        );

        Page<ToolInventory> inventoryPage = toolInventoryRepository.findAll(spec, pageable);

        return inventoryPage.map(inventory -> ToolInventoryDTO.builder()
                .toolId(inventory.getTool().getId())
                .toolName(inventory.getTool().getName())
                .toolImageUrl(inventory.getTool().getToolImageUrl())
                .totalQuantity(inventory.getTotalQuantity())
                .availableQuantity(inventory.getAvailableQuantity())
                .brokenQuantity(inventory.getBrokenQuantity())
                .minimumThreshold(inventory.getMinimumThreshold())
                .fineAmount(inventory.getTool().getFineAmount())
                .returnPeriod(inventory.getTool().getReturnPeriod())
                .toolCategory(inventory.getTool().getCategory())
                .isPerishable(inventory.getTool().getIsPerishable())
                .toolCribId(inventory.getToolCrib().getId())
                .toolCribName(inventory.getToolCrib().getName())
                .build()
        );
    }

    //Worker can also view their own inventory
    @Override
    public Page<ToolInventoryDTO> getInventoryForWorker(String email, String name, Boolean isPerishable,
                                                        List<ToolCategory> category, Double minPrice,
                                                        Double maxPrice, int page, int size) {

        User worker = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Workstation workstation = workstationRepository.findByWorker(worker)
                .orElseThrow(() -> new UserNotFoundException("Workstation not found for worker"));

        Workplace workplace = workstation.getWorkplace();

        ToolCrib toolCrib = toolCribRepository.findByWorkplace(workplace)
                .orElseThrow(() -> new UserNotFoundException("Tool crib not found for workplace"));

        Pageable pageable = PageRequest.of(page, size);

        Specification<ToolInventory> spec = ToolInventorySpecification.filterInventory(
                toolCrib.getId(), name, isPerishable, category, minPrice, maxPrice
        );

        Page<ToolInventory> inventoryPage = toolInventoryRepository.findAll(spec, pageable);

        return inventoryPage.map(inventory -> ToolInventoryDTO.builder()
                .toolId(inventory.getTool().getId())
                .toolCribName(inventory.getToolCrib().getName())
                .toolCribId(inventory.getToolCrib().getId())
                .toolName(inventory.getTool().getName())
                .toolImageUrl(inventory.getTool().getToolImageUrl())
                .availableQuantity(inventory.getAvailableQuantity())
                .totalQuantity(inventory.getTotalQuantity())
                .minimumThreshold(inventory.getMinimumThreshold())
                .fineAmount(inventory.getTool().getFineAmount())
                .brokenQuantity(inventory.getBrokenQuantity())
                .returnPeriod(inventory.getTool().getReturnPeriod())
                .toolCategory(inventory.getTool().getCategory())
                .isPerishable(inventory.getTool().getIsPerishable())
                .build()
        );
    }

    //Tool Crib Manager can also view their own inventory
    @Override
    public Page<ToolInventoryDTO> getInventoryForToolCribManager
            (String email, String name, List<ToolCategory> category,
             Boolean isPerishable, Double minPrice, Double maxPrice,
             int page, int size) {

        User toolCribManager = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        ToolCrib toolCrib = toolCribRepository.findByToolCribManagersContaining(toolCribManager)
                .orElseThrow(() -> new UserNotFoundException("No ToolCrib assigned to ToolCribManager"));

        Specification<ToolInventory> spec = ToolInventorySpecification.filterInventoryByToolProperties(
                toolCrib.getId(), name, category, isPerishable, minPrice, maxPrice
        );

        Pageable pageable = PageRequest.of(page, size);
        Page<ToolInventory> inventoryPage = toolInventoryRepository.findAll(spec, pageable);

        return inventoryPage.map(inventory -> ToolInventoryDTO.builder()
                .toolId(inventory.getTool().getId())
                .toolName(inventory.getTool().getName())
                .toolImageUrl(inventory.getTool().getToolImageUrl())
                .totalQuantity(inventory.getTotalQuantity())
                .availableQuantity(inventory.getAvailableQuantity())
                .brokenQuantity(inventory.getBrokenQuantity())
                .minimumThreshold(inventory.getMinimumThreshold())
                .fineAmount(inventory.getTool().getFineAmount())
                .returnPeriod(inventory.getTool().getReturnPeriod())
                .toolCategory(inventory.getTool().getCategory())
                .isPerishable(inventory.getTool().getIsPerishable())
                .toolCribId(inventory.getToolCrib().getId())
                .toolCribName(inventory.getToolCrib().getName())
                .build()
        );
    }
}
