package com.coditas.tool.management.system.repository;

import com.coditas.tool.management.system.entity.Facility;
import com.coditas.tool.management.system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long>, JpaSpecificationExecutor<Facility> {

//    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.role = 'ROLE_FACILITY_MANAGER'")
//    List<User> findAllFacilities();

    Optional<Facility> findByFacilityManager(User user);

        List<Facility> findByActiveTrue();

}