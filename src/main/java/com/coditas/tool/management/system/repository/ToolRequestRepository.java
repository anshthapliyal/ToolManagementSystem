package com.coditas.tool.management.system.repository;

import com.coditas.tool.management.system.entity.ToolRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ToolRequestRepository extends JpaRepository<ToolRequest, Long> {
}
