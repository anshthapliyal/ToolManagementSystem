package com.coditas.tool.management.system.repository;

import com.coditas.tool.management.system.dto.tool.TopToolReportDTO;
import com.coditas.tool.management.system.entity.Tool;
import com.coditas.tool.management.system.entity.ToolCrib;
import com.coditas.tool.management.system.entity.ToolInventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ToolInventoryRepository extends JpaRepository<ToolInventory, Long> {
    Optional<ToolInventory> findByToolCribAndTool(ToolCrib toolCrib, Tool tool);
    List<ToolInventory> findByToolCribId(Long toolCribId);

    Page<ToolInventory> findByToolCribIdAndToolNameContainingIgnoreCase(Long toolCribId,
                                                                        String toolName, Pageable pageable);

    Page<ToolInventory> findByToolCribAndTool_NameContainingIgnoreCase(ToolCrib toolCrib,
                                                                       String search, Pageable pageable);

    Page<ToolInventory> findByToolCrib(ToolCrib toolCrib, Pageable pageable);

    Page<ToolInventory> findAll(Specification<ToolInventory> spec, Pageable pageable);

    @Query("""
    SELECT new com.coditas.tool.management.system.dto.tool.TopToolReportDTO(t.name, 
    COALESCE(SUM(ti.brokenQuantity), 0)) 
    FROM ToolInventory ti
    JOIN ti.tool t
    GROUP BY t.name
    ORDER BY SUM(ti.brokenQuantity) DESC
    """)
    List<TopToolReportDTO> findTopBrokenTools(); //report

}