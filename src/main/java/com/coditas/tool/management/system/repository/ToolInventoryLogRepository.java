package com.coditas.tool.management.system.repository;

import com.coditas.tool.management.system.entity.ToolInventoryLog;
import com.coditas.tool.management.system.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ToolInventoryLogRepository extends JpaRepository<ToolInventoryLog, Long> {
    List<ToolInventoryLog> findByAssignedByOrderByAssignedAtDesc(User assignedBy);

    Page<ToolInventoryLog> findByAssignedBy(User user, Pageable pageable);

    Page<ToolInventoryLog> findByAssignedByAndAssignedAtBetween(User user, LocalDateTime start,
                                                                LocalDateTime end, Pageable pageable);
}
