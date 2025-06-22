package com.coditas.tool.management.system.service.impl;

import com.coditas.tool.management.system.dto.sharedResponse.SuccessResponse;
import com.coditas.tool.management.system.specification.FacilitySpecification;
import com.coditas.tool.management.system.dto.premises.FacilityDTO;
import com.coditas.tool.management.system.dto.premises.FacilityListDTO;
import com.coditas.tool.management.system.dto.premises.WorkplaceDTO;
import com.coditas.tool.management.system.entity.Facility;
import com.coditas.tool.management.system.entity.User;
import com.coditas.tool.management.system.exception.PremiseNotFoundException;
import com.coditas.tool.management.system.repository.FacilityRepository;
import com.coditas.tool.management.system.repository.UserRepository;
import com.coditas.tool.management.system.service.FacilityService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FacilityServiceImpl implements FacilityService {

    private final FacilityRepository facilityRepository;
    private final UserRepository userRepository;

    @Autowired
    public FacilityServiceImpl(FacilityRepository facilityRepository,
                               UserRepository userRepository) {
        this.facilityRepository = facilityRepository;
        this.userRepository = userRepository;
    }

    //Method to add Facility
    @Override
    public SuccessResponse createFacility(FacilityDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User owner = userRepository.findByEmail(email).get();

        //facility object, setting the details and then saving it
        Facility facility = Facility.builder()
                .name(dto.getName())
                .address(dto.getAddress())
                .ownerId(owner)
                .build();
        facility.setActive(true);
        if (dto.getFacilityManagerEmail() != null || dto.getFacilityManagerEmail() != "") {
            facility.setFacilityManager(userRepository.findByEmail(dto.getFacilityManagerEmail()).get());
        }
        facilityRepository.save(facility);
        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Facility created successfully")
                .time(LocalDateTime.now())
                .build();
    }

    @Override
    public SuccessResponse updateFacility(long id, FacilityDTO dto) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new PremiseNotFoundException("No Facility found with this id."));
        facility.setName(dto.getName());
        facility.setAddress(dto.getAddress());
        if (dto.getFacilityManagerEmail() != null || dto.getFacilityManagerEmail() != "") {
            facility.setFacilityManager(userRepository.findByEmail(dto.getFacilityManagerEmail()).get());
        }
        facilityRepository.save(facility);
        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Facility details has been updated successfully.")
                .time(LocalDateTime.now())
                .build();
    }


    @Override
    public SuccessResponse deleteFacility(long id) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new PremiseNotFoundException("Facility Not Found with given id"));

        facility.setActive(false);
        facilityRepository.save(facility);
        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Facility deleted successfully.")
                .time(LocalDateTime.now())
                .build();

    }

    @Override
    public Page<FacilityListDTO> findAllFacilities(int page, int size, String search, List<String> fields) {
        Pageable pageable = PageRequest.of(page, size);

        Specification<Facility> spec = Specification
                .where(FacilitySpecification.isActive())
                .and(FacilitySpecification.searchByFields(search, fields));

        Page<Facility> facilityPage = facilityRepository.findAll(spec, pageable);

        List<FacilityListDTO> dtoList = facilityPage.getContent().stream().map(facility -> {
            FacilityListDTO dto = new FacilityListDTO();
            dto.setId(facility.getId());
            dto.setName(facility.getName());
            dto.setAddress(facility.getAddress());
            dto.setCreatedAt(facility.getCreatedAt());

            if (facility.getFacilityManager() != null) {
                dto.setFacilityManagerName(facility.getFacilityManager().getName());
                dto.setFacilityManagerEmail(facility.getFacilityManager().getEmail());
            }

            if (facility.getWorkplaces() != null) {
                List<WorkplaceDTO> workplaceDTOs = facility.getWorkplaces()
                        .stream()
                        .map(workplace -> {
                            WorkplaceDTO wdto = new WorkplaceDTO();
                            wdto.setName(workplace.getName());
                            if (workplace.getWorkplaceManager() != null) {
                                wdto.setWorkplaceManagerName(workplace.getWorkplaceManager().getName());
                                wdto.setWorkplaceManagerEmail(workplace.getWorkplaceManager().getEmail());
                            }
                            return wdto;
                        })
                        .collect(Collectors.toList());

                dto.setWorkplaceDTOList(workplaceDTOs);
            }

            return dto;
        }).collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, facilityPage.getTotalElements());
    }
}


    //Method to Assign Facility to the facility manager
//    @Override
//    public void assignFacilityToManager(Long facilityId, String managerEmail){
//        //Fetch the facility
//        Facility facility = facilityRepository.findById(facilityId)
//                .orElseThrow(() -> new RuntimeException("Facility not found"));
//
//        //Fetch the manager
//        User managerUser = userRepository.findByEmail(managerEmail)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        //Set the facility manager and facility
//        facility.setFacilityManager(managerUser);
//        facilityRepository.save(facility);
//    }

