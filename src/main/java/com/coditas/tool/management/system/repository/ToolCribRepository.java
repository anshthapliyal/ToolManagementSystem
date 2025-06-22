package com.coditas.tool.management.system.repository;

import com.coditas.tool.management.system.entity.ToolCrib;
import com.coditas.tool.management.system.entity.User;
import com.coditas.tool.management.system.entity.Workplace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ToolCribRepository extends JpaRepository<ToolCrib, Long>, JpaSpecificationExecutor<ToolCrib> {
    Optional<ToolCrib> findByWorkplaceId(Long workplaceId);

    Optional<ToolCrib> findByWorkplace(Workplace workplace);

    @Query("SELECT t FROM ToolCrib t JOIN t.toolCribManagers m WHERE m.email = :email")
    Optional<ToolCrib> findByToolCribManagerEmail(@Param("email") String email);

    Optional<ToolCrib> findByToolCribManagersContaining(User toolCribManager);
}
