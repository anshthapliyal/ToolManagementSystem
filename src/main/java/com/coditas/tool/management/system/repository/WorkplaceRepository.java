package com.coditas.tool.management.system.repository;


import com.coditas.tool.management.system.entity.User;
import com.coditas.tool.management.system.entity.Workplace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkplaceRepository extends JpaRepository<Workplace, Long>, JpaSpecificationExecutor<Workplace> {

    Optional<Workplace> findByWorkplaceManager(User user);
}
