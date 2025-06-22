package com.coditas.tool.management.system.service.impl;


import com.coditas.tool.management.system.dto.sharedResponse.SuccessResponse;
import com.coditas.tool.management.system.dto.premises.WorkstationDTO;
import com.coditas.tool.management.system.entity.*;
import com.coditas.tool.management.system.exception.PremiseNotFoundException;
import com.coditas.tool.management.system.exception.UserNotFoundException;
import com.coditas.tool.management.system.repository.UserRepository;
import com.coditas.tool.management.system.repository.WorkplaceRepository;
import com.coditas.tool.management.system.repository.WorkstationRepository;
import com.coditas.tool.management.system.service.WorkstationService;
import com.coditas.tool.management.system.specification.WorkstationSpecification;
import jakarta.validation.Valid;
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
import java.util.UUID;

@Service
public class WorkstationServiceImpl implements WorkstationService {

    private final UserRepository userRepository;
    private final WorkplaceRepository workplaceRepository;
    private final WorkstationRepository workstationRepository;


    @Autowired
    public WorkstationServiceImpl(UserRepository userRepository, WorkplaceRepository workplaceRepository, WorkstationRepository workstationRepository) {
        this.userRepository = userRepository;
        this.workplaceRepository = workplaceRepository;
        this.workstationRepository = workstationRepository;
    }

    @Override
    public SuccessResponse createWorkstation(@Valid WorkstationDTO workstationDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found."));

        // Fetch the workplace of WorkplaceManager)
        Workplace workplace = workplaceRepository.findByWorkplaceManager(creator)
                .orElseThrow(() -> new PremiseNotFoundException
                        ("No workplace found under the authenticated workplace manager."));

        // worker
        User worker = userRepository.findByEmail(workstationDTO.getWorkerEmail())
                .orElseThrow(() ->
                        new UserNotFoundException("Worker not found with email: "
                                + workstationDTO.getWorkerEmail()));

        //worker is available
        workstationRepository.findByWorker(worker).ifPresent(ws -> {
            throw new IllegalStateException("This worker is already assigned to another workstation.");
        });

        // Generate stationCode
        String stationCode = "WS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        //Random naming to give authentic feel

        Workstation workstation = Workstation.builder()
                .stationCode(stationCode)
                .worker(worker)
                .workplace(workplace)
                .build();
        WorkstationDTO workstationDTO1 =
                WorkstationDTO.builder()
                                .workerEmail(workstation.getWorker().getEmail())
                                        .name(stationCode).build();

        workstationRepository.save(workstation);

        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Workstation created successfully")
                .data(workstationDTO1)
                .time(LocalDateTime.now())
                .build();
    }

    @Override
    public SuccessResponse updateWorkstation(long id, WorkstationDTO workstationDTO) {
        Workstation workstation = workstationRepository.findById(id)
                .orElseThrow(() -> new PremiseNotFoundException("No Workstation found with this ID."));

        // Update name if given
        if (workstationDTO.getName() != null && !workstationDTO.getName().trim().isEmpty()) {
            workstation.setStationCode(workstationDTO.getName());
        }

        String workerEmail = workstationDTO.getWorkerEmail();
        if (workerEmail != null && !workerEmail.trim().isEmpty()) {
            User newWorker = userRepository.findByEmail(workerEmail)
                    .orElseThrow(() ->
                            new UserNotFoundException("No user found with the given email: " + workerEmail));

            //worker should be free
            workstationRepository.findByWorker(newWorker).ifPresent(existingWs -> {
                if (!existingWs.getId().equals(id)) {
                    throw new IllegalStateException("This worker is already assigned to another workstation.");
                }
            });

            workstation.setWorker(newWorker);
        }

        workstationRepository.save(workstation);

        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Workstation details have been updated successfully.")
                .data(workstationDTO)
                .time(LocalDateTime.now())
                .build();
    }

    @Override
    public SuccessResponse deleteWorkstation(long id) {
        Workstation workstation = workstationRepository.findById(id)
                .orElseThrow(() -> new PremiseNotFoundException("Workstation not found with the given ID."));

        // Break relationship/linkage with worker(for not getting db error)
        if (workstation.getWorker() != null) {
            workstation.setWorker(null);
        }

        workstationRepository.delete(workstation);

        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Workstation deleted successfully (and Worker relationship removed).")
                .time(LocalDateTime.now())
                .build();
    }

    public Page<WorkstationDTO> findAllWorkstations(int page, int size, String managerEmail,
                                                    String search, List<String> fields) {
        Pageable pageable = PageRequest.of(page, size);

        Workplace workplace = workplaceRepository.findByWorkplaceManager(
                userRepository.findByEmail(managerEmail)
                        .orElseThrow(() ->
                                new UserNotFoundException("Manager not found with email: " + managerEmail))
        ).orElseThrow(() ->
                new PremiseNotFoundException("No workplace found for the logged-in manager."));

        Specification<Workstation> spec = Specification
                .where(WorkstationSpecification.belongsToWorkplace(workplace.getId()))
                .and(WorkstationSpecification.filterByFields(search, fields));

        Page<Workstation> workstationPage = workstationRepository.findAll(spec, pageable);

        List<WorkstationDTO> dtos = workstationPage.getContent().stream().map(workstation -> WorkstationDTO.builder()
                .id(workstation.getId())
                .name(workstation.getStationCode())
                .workerEmail(workstation.getWorker() != null ? workstation.getWorker().getEmail() : null)
                .workerName(workstation.getWorker() != null ? workstation.getWorker().getName() : null)
                .build()).toList();

        return new PageImpl<>(dtos, pageable, workstationPage.getTotalElements());
    }

}
