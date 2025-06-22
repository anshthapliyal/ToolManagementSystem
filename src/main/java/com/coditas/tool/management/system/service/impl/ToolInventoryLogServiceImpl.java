package com.coditas.tool.management.system.service.impl;

import com.coditas.tool.management.system.dto.tool.ToolInventoryLogDTO;
import com.coditas.tool.management.system.entity.ToolInventoryLog;
import com.coditas.tool.management.system.entity.User;
import com.coditas.tool.management.system.repository.ToolInventoryLogRepository;
import com.coditas.tool.management.system.repository.UserRepository;
import com.coditas.tool.management.system.service.ToolInventoryLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ToolInventoryLogServiceImpl implements ToolInventoryLogService {

    private final ToolInventoryLogRepository logRepository;
    private final UserRepository userRepository;

    @Autowired
    public ToolInventoryLogServiceImpl(ToolInventoryLogRepository logRepository,
                                       UserRepository userRepository) {
        this.logRepository = logRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Page<ToolInventoryLogDTO> getLogs(int page, int size,
                                             LocalDateTime minDateTime, LocalDateTime maxDateTime) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("assignedAt").descending());

        Page<ToolInventoryLog> logs = logRepository
                .findByAssignedByAndAssignedAtBetween(user,
                        minDateTime != null ? minDateTime : LocalDateTime.MIN,
                        maxDateTime != null ? maxDateTime : LocalDateTime.now(),
                        pageable);

        return logs.map(log -> ToolInventoryLogDTO.builder()
                .toolName(log.getTool().getName())
                .workplaceName(log.getWorkplace().getName())
                .toolCribName(log.getToolCrib().getName())
                .assignedBy(log.getAssignedBy().getEmail())
                .quantityAssigned(log.getQuantityAssigned())
                .assignedAt(log.getAssignedAt())
                .build());
    }

}