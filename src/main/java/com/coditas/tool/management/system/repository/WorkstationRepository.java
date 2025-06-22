package com.coditas.tool.management.system.repository;

import com.coditas.tool.management.system.entity.User;
import com.coditas.tool.management.system.entity.Workplace;
import com.coditas.tool.management.system.entity.Workstation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkstationRepository extends JpaRepository<Workstation, Long> {
    Optional<Workstation> findByWorker(User user);

    Page<Workstation> findAllByWorkplace(Workplace workplace, Pageable pageable);

    Page<Workstation> findAll(Specification<Workstation> spec, Pageable pageable);
}
