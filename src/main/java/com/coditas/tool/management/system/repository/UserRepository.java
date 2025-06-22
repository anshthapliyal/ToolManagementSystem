package com.coditas.tool.management.system.repository;

import com.coditas.tool.management.system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByName(String name);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    boolean existsByName(String name);

    boolean existsByEmail(String email);


    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.role = 'ROLE_FACILITYMANAGER' AND u.active = true")
    List<User> findAllFacilityManagers();

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.role = :role AND u.active = true")
    List<User> findByRoleName(@Param("role") String role);

}