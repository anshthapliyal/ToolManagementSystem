package com.coditas.tool.management.system.service.impl;

import com.coditas.tool.management.system.constant.RequestStatus;
import com.coditas.tool.management.system.constant.ReturnStatus;
import com.coditas.tool.management.system.constant.ToolCategory;
import com.coditas.tool.management.system.dto.sharedResponse.SuccessResponse;
import com.coditas.tool.management.system.dto.tool.ToolRequestCreateDTO;
import com.coditas.tool.management.system.dto.tool.ToolRequestItemDTO;
import com.coditas.tool.management.system.dto.tool.ToolReturnRequestDTO;
import com.coditas.tool.management.system.dto.tool.UnreturnedToolWorkerDTO;
import com.coditas.tool.management.system.entity.*;
import com.coditas.tool.management.system.exception.PremiseNotFoundException;
import com.coditas.tool.management.system.exception.ResourceNotFoundException;
import com.coditas.tool.management.system.exception.UserNotFoundException;
import com.coditas.tool.management.system.repository.*;
import com.coditas.tool.management.system.service.EmailService;
import com.coditas.tool.management.system.service.ToolRequestService;
import com.coditas.tool.management.system.specification.ToolRequestItemSpecifications;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ToolRequestServiceImpl implements ToolRequestService {

    private final ToolRequestItemRepository toolRequestItemRepository;
    private final UserRepository userRepository;
    private final WorkstationRepository workstationRepository;
    private final ToolCribRepository toolCribRepository;
    private final ToolRequestRepository toolRequestRepository;
    private final ToolRepository toolRepository;
    private final ToolInventoryRepository toolInventoryRepository;
    private final EmailService emailService;

    @Autowired
    public ToolRequestServiceImpl
            (ToolRequestItemRepository toolRequestItemRepository, UserRepository userRepository,
             WorkstationRepository workstationRepository, ToolCribRepository toolCribRepository,
             ToolRequestRepository toolRequestRepository, ToolRepository toolRepository,
             ToolInventoryRepository toolInventoryRepository, EmailService emailService) {
        this.toolRequestItemRepository = toolRequestItemRepository;
        this.userRepository = userRepository;
        this.workstationRepository = workstationRepository;
        this.toolCribRepository = toolCribRepository;
        this.toolRequestRepository = toolRequestRepository;
        this.toolRepository = toolRepository;
        this.toolInventoryRepository = toolInventoryRepository;
        this.emailService = emailService;
    }

    @Override
    public Page<ToolRequestItemDTO> getSpecialRequestsForManager(
            int page, int size,
            String search, List<String> fields,
            LocalDateTime startDateTime, LocalDateTime endDateTime) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User manager = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Authenticated Manager not found."));

        Pageable pageable = PageRequest.of(page, size);

        Specification<ToolRequestItem> spec = Specification
                .where(ToolRequestItemSpecifications.hasSpecialCategory())
                .and(ToolRequestItemSpecifications.isApprovedByWpmOrNull(manager.getId()));

        // AND-based search logic using fields
        if (search != null && fields != null && !fields.isEmpty()) {
            for (String field : fields) {
                switch (field) {
                    case "toolName" -> spec = spec.and(ToolRequestItemSpecifications.toolNameLike(search));
                    case "workerName" -> spec = spec.and(ToolRequestItemSpecifications.workerEmailLike(search));
                    default -> throw new IllegalArgumentException("Unsupported field: " + field);
                }
            }
        }

        if (startDateTime != null) {
            spec = spec.and(ToolRequestItemSpecifications.requestDateAfter(startDateTime));
        }

        if (endDateTime != null) {
            spec = spec.and(ToolRequestItemSpecifications.requestDateBefore(endDateTime));
        }

        Page<ToolRequestItem> itemsPage = toolRequestItemRepository.findAll(spec, pageable);

        return itemsPage.map(item -> ToolRequestItemDTO.builder()
                .toolId(item.getTool().getId())
                .workerId(item.getToolRequest().getWorker().getId())
                .requestItemId(item.getId())
                .toolName(item.getTool().getName())
                .reqQuantity(item.getReqQuantity())
                .approvalStatus(item.getApprovalStatus())
                .requestDate(item.getToolRequest().getRequestDate())
                .returnDate(item.getReturnDate())
                .workerName(item.getToolRequest().getWorker().getName())
                .isPerishable(item.getTool().getIsPerishable())
                .toolCategory(item.getTool().getCategory())
                .returnStatus(item.getReturnStatus())
                .build());
    }



    @Override
    public SuccessResponse decideSpecialRequest(Long itemId, boolean approve) throws BadRequestException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        ToolRequestItem item = toolRequestItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("ToolRequestItem not found"));

        if (item.getApprovalStatus() == RequestStatus.APPROVED ||
                item.getApprovalStatus() == RequestStatus.REJECTED) {
            throw new BadRequestException("Request has already been processed");
        }

        Tool tool = item.getTool();
        if (tool.getCategory() != ToolCategory.SPECIAL) {
            throw new BadRequestException("Only special category tools are handled here");
        }

        User wpm = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Workplace Manager not found"));

        ToolRequest request = item.getToolRequest();
        Workplace workplace = request.getWorkplace();

        ToolCrib toolCrib = toolCribRepository.findByWorkplace(workplace)
                .orElseThrow(() -> new ResourceNotFoundException("ToolCrib not found"));

        ToolInventory inventory = toolInventoryRepository.findByToolCribAndTool(toolCrib, tool)
                .orElseThrow(() -> new ResourceNotFoundException("Tool not found in ToolCrib"));

        item.setApprovedByWpm(wpm);
        item.setApprovalStatus(approve ? RequestStatus.APPROVED : RequestStatus.REJECTED);

        if (approve) {
            long updatedQty = inventory.getAvailableQuantity() - item.getReqQuantity();
            if (updatedQty < 0) {
                throw new BadRequestException("Insufficient inventory for the tool");
            }

            inventory.setAvailableQuantity(updatedQty);
            toolInventoryRepository.save(inventory);

            if (Boolean.TRUE.equals(tool.getIsPerishable())) {
                item.setReturnStatus(ReturnStatus.UNRETURNABLE);
                item.setReturnDate(null);
            } else {
                item.setReturnStatus(ReturnStatus.PENDING);

                //if returnPeriod given-> returnPeriod+Today's date
                //else -> today's date + 5 days
                LocalDateTime computedReturnDate = (tool.getReturnPeriod() != null)
                        ? LocalDateTime.now().plusDays(tool.getReturnPeriod())
                        : LocalDateTime.now().plusDays(5);

                item.setReturnDate(computedReturnDate);
            }
        }

        toolRequestItemRepository.save(item);

        return SuccessResponse.builder()
                .message("Tool request has been " + (approve ? "approved" : "rejected") + " successfully.")
                .build();
    }


    @Override
    @Transactional
    public SuccessResponse createToolRequest(String email, ToolRequestCreateDTO requestDTO) {
        User worker = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found"));

        Workstation workstation = workstationRepository.findByWorker(worker)
                .orElseThrow(() -> new ResourceNotFoundException("Worker is not assigned to any workstation"));

        Workplace workplace = workstation.getWorkplace();

        ToolCrib toolCrib = toolCribRepository.findByWorkplace(workplace)
                .orElseThrow(() -> new ResourceNotFoundException("ToolCrib not found for workplace"));

        LocalDateTime requestDate = LocalDateTime.now();
        LocalDateTime returnDate = requestDate.plusDays(5);

        ToolRequest toolRequest = ToolRequest.builder()
                .worker(worker)
                .workplace(workplace)
                .requestDate(requestDate)
                .approvalDate(null)
                .returnDate(returnDate)
                .requestStatus(RequestStatus.PENDING)
                .build();

        toolRequest = toolRequestRepository.save(toolRequest);


        for (ToolRequestItemDTO itemDTO : requestDTO.getItems()) {
            Tool tool = toolRepository.findById(itemDTO.getToolId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tool not found"));

            ToolInventory inventory = toolInventoryRepository.findByToolCribAndTool(toolCrib, tool)
                    .orElseThrow(() -> new ResourceNotFoundException("Tool not available in ToolCrib"));

            if (itemDTO.getReqQuantity() > inventory.getAvailableQuantity()) {
                throw new IllegalArgumentException("Requested quantity exceeds available for tool: " + tool.getName());
            }


            ToolRequestItem toolRequestItem = ToolRequestItem.builder()
                    .toolRequest(toolRequest)
                    .tool(tool)
                    .reqQuantity(itemDTO.getReqQuantity())
                    .retQuantity(0L)
                    .brkQuantity(0L)
                    .approvalStatus(RequestStatus.PENDING)
                    .returnStatus(ReturnStatus.PENDING)
                    .fine(0L)
                    .build();

            toolRequestItemRepository.save(toolRequestItem);

            itemDTO.setToolName(tool.getName());
            itemDTO.setApprovalStatus(RequestStatus.PENDING);
            itemDTO.setRequestDate(requestDate);
            itemDTO.setReturnDate(returnDate);
            itemDTO.setWorkerName(worker.getName());
        }

        return SuccessResponse.builder()
                .message("Tool Request Successfully Raised.")
                .data(toolRequest)
                .build();
    }

    @Override
    public Page<ToolRequestItemDTO> getToolRequestsByWorker(String toolName, String approvalStatus, int page, int size) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User worker = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found"));

        Pageable pageable = PageRequest.of(page, size);

        Specification<ToolRequestItem> spec = Specification.where(
                (root, query, cb) 
                        -> cb.equal(root.get("toolRequest").get("worker"), worker)
        );

        if (toolName != null && !toolName.isBlank()) {
            spec = spec.and(ToolRequestItemSpecifications.toolNameLike(toolName));
        }

        if (approvalStatus != null && !approvalStatus.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("approvalStatus"), approvalStatus));
        }

        Page<ToolRequestItem> requestItems = toolRequestItemRepository.findAll(spec, pageable);

        return requestItems.map(item -> ToolRequestItemDTO.builder()
                .requestItemId(item.getId())
                .toolId(item.getTool().getId())
                .toolName(item.getTool().getName())
                .reqQuantity(item.getReqQuantity())
                .approvalStatus(item.getApprovalStatus())
                .returnStatus(item.getReturnStatus())
                .requestDate(item.getToolRequest().getRequestDate())
                .returnDate(item.getToolRequest().getReturnDate())
                .fine(item.getFine())
                .isPerishable(item.getTool().getIsPerishable())
                .toolCategory(item.getTool().getCategory())
                .workerName(worker.getName())
                .workerId(worker.getId())
                .build());
    }

    @Override
    public SuccessResponse decideNormalToolRequest(Long itemId, boolean approve) throws BadRequestException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        ToolRequestItem item = toolRequestItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("ToolRequestItem not found"));

        if (item.getApprovalStatus() == RequestStatus.APPROVED ||
                item.getApprovalStatus() == RequestStatus.REJECTED) {
            throw new BadRequestException("Request has already been processed");
        }

        Tool tool = item.getTool();

        // Only NORMAL tools allowed
        if (tool.getCategory() != ToolCategory.NORMAL) {
            throw new BadRequestException("Only NORMAL category tools can be handled by ToolCrib Manager.");
        }

        User cribManager = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated ToolCrib Manager not found"));

        ToolRequest request = item.getToolRequest();
        Workplace workplace = request.getWorkplace();

        ToolCrib toolCrib = toolCribRepository.findByWorkplace(workplace)
                .orElseThrow(() -> new ResourceNotFoundException("ToolCrib not found for the workplace"));

        ToolInventory inventory = toolInventoryRepository.findByToolCribAndTool(toolCrib, tool)
                .orElseThrow(() -> new ResourceNotFoundException("Tool not found in ToolCrib"));

        item.setApprovedByCrib(cribManager);
        item.setApprovalStatus(approve ? RequestStatus.APPROVED : RequestStatus.REJECTED);

        if (approve) {
            long updatedQty = inventory.getAvailableQuantity() - item.getReqQuantity();
            if (updatedQty < 0) {
                throw new BadRequestException("Insufficient quantity available in ToolCrib");
            }

            inventory.setAvailableQuantity(updatedQty);
            toolInventoryRepository.save(inventory);

            if (Boolean.TRUE.equals(tool.getIsPerishable())) {
                item.setReturnStatus(ReturnStatus.UNRETURNABLE);
                item.setReturnDate(null);
                item.setRetQuantity(0L);
            } else {
                item.setReturnStatus(ReturnStatus.PENDING);

                //if returnPeriod is set, use it. Else, using ToolRequest's default returnDate(5 days)
                LocalDateTime computedReturnDate = (tool.getReturnPeriod() != null)
                        ? LocalDateTime.now().plusDays(tool.getReturnPeriod())
                        : LocalDateTime.now().plusDays(5);

                item.setReturnDate(computedReturnDate);
            }
        }

        toolRequestItemRepository.save(item);

        return SuccessResponse.builder()
                .message("Tool request has been " + (approve ? "approved" : "rejected") + " successfully.")
                .build();
    }


    @Override
    public Page<ToolRequestItemDTO> getAllToolRequestsForCribManager(
            int page, int size,
            String search,
            List<String> fields,
            LocalDateTime startDateTime, LocalDateTime endDateTime) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User cribManager = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Authenticated ToolCrib Manager not found."));

        Pageable pageable = PageRequest.of(page, size);

        ToolCrib toolCrib = toolCribRepository.findByToolCribManagerEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No ToolCrib found for this manager"));

        Workplace workplace = toolCrib.getWorkplace();

        Specification<ToolRequestItem> spec = Specification
                .where(ToolRequestItemSpecifications.hasWorkplace(workplace.getId()));

        if (search != null && !search.isBlank() && fields != null && !fields.isEmpty()) {
            for (String field : fields) {
                switch (field.trim().toLowerCase()) {
                    case "toolname":
                        spec = spec.and(ToolRequestItemSpecifications.toolNameLike(search));
                        break;
                    case "workername":
                        spec = spec.and(ToolRequestItemSpecifications.workerEmailLike(search));
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported search field: " + field);
                }
            }
        }

        if (startDateTime != null) {
            spec = spec.and(ToolRequestItemSpecifications.requestDateAfter(startDateTime));
        }

        if (endDateTime != null) {
            spec = spec.and(ToolRequestItemSpecifications.requestDateBefore(endDateTime));
        }

        Page<ToolRequestItem> itemsPage = toolRequestItemRepository.findAll(spec, pageable);

        return itemsPage.map(item -> ToolRequestItemDTO.builder()
                .toolId(item.getTool().getId())
                .requestItemId(item.getId())
                .toolName(item.getTool().getName())
                .reqQuantity(item.getReqQuantity())
                .approvalStatus(item.getApprovalStatus())
                .requestDate(item.getToolRequest().getRequestDate())
                .returnDate(item.getReturnDate())
                .workerName(item.getToolRequest().getWorker().getName())
                .workerId(item.getToolRequest().getWorker().getId())
                .isPerishable(item.getTool().getIsPerishable())
                .toolCategory(item.getTool().getCategory())
                .returnStatus(item.getReturnStatus())
                .build());
    }

    @Override
    public SuccessResponse returnTool(ToolReturnRequestDTO dto) throws BadRequestException {
        ToolRequestItem item = toolRequestItemRepository.findById(dto.getRequestItemId())
                .orElseThrow(() -> new PremiseNotFoundException("ToolRequestItem not found"));

        if (ReturnStatus.RETURNED.equals(item.getReturnStatus())) {
            throw new IllegalArgumentException("Tool already returned.");
        }

        if (Boolean.TRUE.equals(item.getTool().getIsPerishable())) {
            if (!ReturnStatus.UNRETURNABLE.equals(item.getReturnStatus())) {
                item.setReturnStatus(ReturnStatus.UNRETURNABLE);
                toolRequestItemRepository.save(item);
            }
            throw new BadRequestException("Perishable tools cannot be returned.");
        }

        if (!RequestStatus.APPROVED.equals(item.getApprovalStatus())) {
            throw new BadRequestException("Tool must be approved before it can be returned.");
        }

        long expectedQty = item.getReqQuantity();
        long returnedQty = dto.getReturnQuantity();

        if (returnedQty < 0 || returnedQty > expectedQty) {
            throw new BadRequestException("Returned quantity must be between 0 and " + expectedQty + ".");
        }

        long brokenQty = expectedQty - returnedQty;
        long fine = 0;

        LocalDateTime expectedReturnDate = item.getToolRequest().getReturnDate();
        LocalDateTime actualReturnDate = dto.getActualReturnDate();

        if (actualReturnDate.isAfter(expectedReturnDate)) {
            long daysLate = ChronoUnit.DAYS.between(expectedReturnDate.toLocalDate(), actualReturnDate.toLocalDate());
            fine += daysLate * item.getTool().getFineAmount();
        }

        if (brokenQty > 0) {
            fine += brokenQty * item.getTool().getFineAmount();
        }

        item.setRetQuantity(returnedQty);
        item.setBrkQuantity(brokenQty);
        item.setFine(fine);
        item.setReturnDate(actualReturnDate);
        item.setReturnStatus(ReturnStatus.RETURNED);
        toolRequestItemRepository.save(item);

        ToolCrib toolCrib = toolCribRepository.findByWorkplaceId(item.getToolRequest().getWorkplace().getId())
                .orElseThrow(() -> new ResourceNotFoundException("ToolCrib not found for workplace ID: "
                        + item.getToolRequest().getWorkplace().getId()));

        ToolInventory inventory = toolInventoryRepository
                .findByToolCribAndTool(toolCrib, item.getTool())
                .orElseThrow(() -> new IllegalStateException("Inventory not found"));

        Long existingBrokenQty = inventory.getBrokenQuantity() != null ? inventory.getBrokenQuantity() : 0L;
        Long existingTotalQty = inventory.getTotalQuantity() != null ? inventory.getTotalQuantity() : 0L;
        Long existingAvailableQty = inventory.getAvailableQuantity() != null ? inventory.getAvailableQuantity() : 0L;

        inventory.setBrokenQuantity(existingBrokenQty + brokenQty);
        inventory.setAvailableQuantity(existingAvailableQty + returnedQty);
        toolInventoryRepository.save(inventory);

        User user = item.getToolRequest().getWorker();
        Tool tool = item.getTool();

        if (fine > 0) {
            String subject = "Hello Worker " + user.getName() + "! Fine Incurred for Tool Return";
            StringBuilder bodyBuilder = new StringBuilder("Dear " + user.getName() + ",\n\n");
            bodyBuilder.append("You have incurred a fine for the tool return. Details are as follows:\n");
            bodyBuilder.append("Tool: ").append(tool.getName()).append("\n");
            bodyBuilder.append("Fine Amount: ₹").append(fine).append("\n");

            if (brokenQty > 0) {
                bodyBuilder.append("Reason: ").append(brokenQty)
                        .append(" tool(s) not returned (counted as broken).\n");
            }

            if (expectedReturnDate != null && actualReturnDate != null &&
                    actualReturnDate.isAfter(expectedReturnDate)) {
                long daysLate = ChronoUnit.DAYS.between(expectedReturnDate.toLocalDate(), actualReturnDate.toLocalDate());
                bodyBuilder.append("Delayed by: ").append(daysLate).append(" day(s).\n");
            }

            bodyBuilder.append("\nPlease ensure timely and complete returns to avoid future fines.\n");
            bodyBuilder.append("Thank you.");
            emailService.sendEmail(user.getEmail(), subject, bodyBuilder.toString());
        } else {
            //All tools returned successfully with no fines — Send success mail
            String subject = "Tool Return Confirmation - All Tools Returned Successfully";
            StringBuilder bodyBuilder = new StringBuilder("Dear ").append(user.getName()).append(",\n\n");

            bodyBuilder.append("We are pleased to inform you that your recent tool return has been processed successfully.\n\n");
            bodyBuilder.append("Tool: ").append(tool.getName()).append("\n");
            bodyBuilder.append("Returned Quantity: ").append(returnedQty).append("\n");
            bodyBuilder.append("Return Date: ").append(actualReturnDate.toLocalDate()).append("\n\n");

            bodyBuilder.append("All tools were returned in good condition and on time.\n");
            bodyBuilder.append("No fines have been applied.\n\n");

            bodyBuilder.append("Thank you for adhering to the return policy and maintaining tool integrity.\n\n");
            bodyBuilder.append("Best regards,\nTool Management System Team");

            emailService.sendEmail(user.getEmail(), subject, bodyBuilder.toString());
        }

        return SuccessResponse.builder()
                .message("Tool has been returned successfully.")
                .build();
    }

    @Override
    public SuccessResponse getUnreturnedToolWorkers() {
        // Get ToolCrib managed by this manager
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        ToolCrib toolCrib = toolCribRepository.findByToolCribManagerEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("ToolCrib not found"));

        Long workplaceId = toolCrib.getWorkplace().getId();
        List<ToolRequestItem> unreturnedItems = toolRequestItemRepository
                .findUnreturnedItemsByWorkplaceId(workplaceId);

        List<UnreturnedToolWorkerDTO> dtoList = unreturnedItems.stream().map(item -> {
            User worker = item.getToolRequest().getWorker();
            return UnreturnedToolWorkerDTO.builder()
                    .workerName(worker.getName())
                    .workerEmail(worker.getEmail())
                    .workerImageUrl(worker.getProfileImageUrl())
                    .toolName(item.getTool().getName())
                    .requestedQuantity(item.getReqQuantity())
                    .returnedQuantity(item.getRetQuantity() != null ? item.getRetQuantity() : 0L)
                    .dueDate(item.getToolRequest().getReturnDate())
                    .toolRequestItemId(item.getId())
                    .build();
        }).collect(Collectors.toList());

        return SuccessResponse.builder()
                .message("Unreturned Items worker details.")
                .data(dtoList)
                .build();

    }
}


