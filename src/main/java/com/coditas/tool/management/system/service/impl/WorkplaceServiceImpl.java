package com.coditas.tool.management.system.service.impl;

import com.coditas.tool.management.system.dto.sharedResponse.SuccessResponse;
import com.coditas.tool.management.system.dto.premises.WorkplaceDTO;
import com.coditas.tool.management.system.dto.premises.WorkplaceListDTO;
import com.coditas.tool.management.system.entity.Facility;
import com.coditas.tool.management.system.entity.ToolCrib;
import com.coditas.tool.management.system.entity.User;
import com.coditas.tool.management.system.entity.Workplace;
import com.coditas.tool.management.system.exception.PremiseNotFoundException;
import com.coditas.tool.management.system.exception.UserNotFoundException;
import com.coditas.tool.management.system.repository.FacilityRepository;
import com.coditas.tool.management.system.repository.ToolCribRepository;
import com.coditas.tool.management.system.repository.UserRepository;
import com.coditas.tool.management.system.repository.WorkplaceRepository;
import com.coditas.tool.management.system.service.WorkplaceService;
import com.coditas.tool.management.system.specification.WorkplaceSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class WorkplaceServiceImpl implements WorkplaceService {

    private final UserRepository userRepository;
    private final FacilityRepository facilityRepository;
    private final WorkplaceRepository workplaceRepository;
    private final ToolCribRepository toolCribRepository;

    public WorkplaceServiceImpl(UserRepository userRepository, FacilityRepository facilityRepository, WorkplaceRepository workplaceRepository, ToolCribRepository toolCribRepository) {
        this.userRepository = userRepository;
        this.facilityRepository = facilityRepository;
        this.workplaceRepository = workplaceRepository;
        this.toolCribRepository = toolCribRepository;
    }

    @Override
    public SuccessResponse createWorkplace(WorkplaceDTO workplaceDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User facilityMngr = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found"));

        // Facility associated with FM
        Facility facility = facilityRepository
                .findByFacilityManager(facilityMngr)
                .orElseThrow(() -> new PremiseNotFoundException(
                        "No Facility found under the jurisdiction of the facility manager."));

        Workplace workplace = Workplace.builder()
                .name(workplaceDTO.getName())
                .facility(facility)
                .build();

        // Workplace Manager setting if email is given
        String workplaceManagerEmail = workplaceDTO.getWorkplaceManagerEmail();
        if (workplaceManagerEmail != null && !workplaceManagerEmail.trim().isEmpty()) {
            User workplaceMngr = userRepository.findByEmail(workplaceManagerEmail)
                    .orElseThrow(() -> new UserNotFoundException(
                            "No workplace manager found with specified email!"));
            workplace.setWorkplaceManager(workplaceMngr);
        }

        workplaceRepository.save(workplace);

        // Automatically creating ToolCrib for this workplace (as they are ONE TO ONE)
        ToolCrib toolCrib = ToolCrib.builder()
                .name(workplace.getName() + "_ToolCrib")
                .workplace(workplace)
                .build();

        toolCribRepository.save(toolCrib);

        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Workplace and Tool Crib created successfully")
                .time(LocalDateTime.now())
                .build();
    }


    @Override
    public SuccessResponse updateWorkplace(long id, WorkplaceDTO workplaceDTO) {
        Workplace workplace = workplaceRepository.findById(id)
                .orElseThrow(() -> new PremiseNotFoundException("No Workplace found with this id."));

        workplace.setName(workplaceDTO.getName());

        String managerEmail = workplaceDTO.getWorkplaceManagerEmail();
        if (managerEmail != null && !managerEmail.trim().isEmpty()) {
            User manager = userRepository.findByEmail(managerEmail)
                    .orElseThrow(() -> new UserNotFoundException("No user found with the given email: " + managerEmail));
            workplace.setWorkplaceManager(manager);
        }

        workplaceRepository.save(workplace);

        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Workplace details have been updated successfully.")
                .time(LocalDateTime.now())
                .build();
    }


    @Override
    public SuccessResponse deleteWorkplace(long id) {
        Workplace workplace = workplaceRepository.findById(id)
                .orElseThrow(() -> new PremiseNotFoundException("Workplace Not Found with given id"));

        // Find ToolCrib
        Optional<ToolCrib> optionalToolCrib = toolCribRepository.findByWorkplace(workplace);

        optionalToolCrib.ifPresent(toolCrib -> {
            toolCrib.setWorkplace(null); //Break the relationship
            toolCribRepository.save(toolCrib);
        });

        workplaceRepository.delete(workplace);

        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Workplace deleted successfully (ToolCrib relationship removed).")
                .time(LocalDateTime.now())
                .build();
    }


    @Override
    public Page<WorkplaceListDTO> findAllWorkplaces(int page, int size, String search, List<String> fields, String email) {
        Pageable pageable = PageRequest.of(page, size);

        Specification<Workplace> spec = Specification
                .where(WorkplaceSpecification.searchByFields(search, fields))
                .and(WorkplaceSpecification.belongsToFacilityManager(email));

        Page<Workplace> workplacePage = workplaceRepository.findAll(spec, pageable);

        List<WorkplaceListDTO> dtoList = workplacePage.getContent().stream().map(workplace -> {
            WorkplaceListDTO dto = new WorkplaceListDTO();
            dto.setId(workplace.getId());
            dto.setName(workplace.getName());
            dto.setCreatedAt(workplace.getCreatedAt());

            if (workplace.getWorkplaceManager() != null) {
                dto.setWorkplaceManagerName(workplace.getWorkplaceManager().getName());
                dto.setWorkplaceManagerEmail(workplace.getWorkplaceManager().getEmail());
            }

            if (workplace.getFacility() != null) {
                dto.setFacilityName(workplace.getFacility().getName());
            }

            return dto;
        }).toList();

        return new PageImpl<>(dtoList, pageable, workplacePage.getTotalElements());
    }

    @Override
    public Page<WorkplaceListDTO> getWorkplacesByFacility(Long facilityId, int page, int size, String search, List<String> fields) {
        Pageable pageable = PageRequest.of(page, size);

        Specification<Workplace> spec = Specification
                .where(WorkplaceSpecification.searchByFields(search, fields))
                .and(WorkplaceSpecification.belongsToFacility(facilityId));

        Page<Workplace> workplacePage = workplaceRepository.findAll(spec, pageable);

        List<WorkplaceListDTO> dtoList = workplacePage.getContent().stream().map(workplace -> WorkplaceListDTO.builder()
                .id(workplace.getId())
                .name(workplace.getName())
                .createdAt(workplace.getCreatedAt())
                .workplaceManagerName(workplace.getWorkplaceManager() != null ? workplace.getWorkplaceManager().getName() : null)
                .workplaceManagerEmail(workplace.getWorkplaceManager() != null ? workplace.getWorkplaceManager().getEmail() : null)
                .facilityName(workplace.getFacility() != null ? workplace.getFacility().getName() : null)
                .build()
        ).toList();

        return new PageImpl<>(dtoList, pageable, workplacePage.getTotalElements());
    }



}
