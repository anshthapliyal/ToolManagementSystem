package com.coditas.tool.management.system.repository;

import com.coditas.tool.management.system.dto.tool.TopToolReportDTO;
import com.coditas.tool.management.system.entity.Tool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ToolRepository extends JpaRepository<Tool, Long>, JpaSpecificationExecutor<Tool> {

    @Query("""
    SELECT new com.coditas.tool.management.system.dto.tool.TopToolReportDTO(t.name, t.price) 
    FROM Tool t
    ORDER BY t.price DESC
    """)
    List<TopToolReportDTO> findTopPricedTools(); //report
}
