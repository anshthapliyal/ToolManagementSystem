package com.coditas.tool.management.system.repository;

import com.coditas.tool.management.system.dto.tool.TopToolReportDTO;
import com.coditas.tool.management.system.entity.ToolRequestItem;
import com.coditas.tool.management.system.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ToolRequestItemRepository extends JpaRepository<ToolRequestItem, Long>,
        JpaSpecificationExecutor<ToolRequestItem> {

    Page<ToolRequestItem> findByToolRequest_Worker(User worker, Pageable pageable);

    @Query("SELECT t FROM ToolRequestItem t " +
            "WHERE t.returnStatus NOT IN ('RETURNED', 'UNRETURNABLE') " +
            "AND t.approvalStatus NOT IN ('PENDING', 'REJECTED') " +
            "AND t.toolRequest.workplace.id = :workplaceId")
    List<ToolRequestItem> findUnreturnedItemsByWorkplaceId(@Param("workplaceId") Long workplaceId);


    @Query("SELECT new com.coditas.tool.management.system.dto.tool.TopToolReportDTO(tr.tool.name" +
            ", SUM(tr.reqQuantity)) " +
            "FROM ToolRequestItem tr " +
            "GROUP BY tr.tool.name " +
            "ORDER BY SUM(tr.reqQuantity) DESC")
    List<TopToolReportDTO> findTopDemandedTools();


}

