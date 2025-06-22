package com.coditas.tool.management.system.service;

import com.coditas.tool.management.system.dto.sharedResponse.SuccessResponse;
import com.coditas.tool.management.system.dto.premises.WorkstationDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

import java.util.List;

public interface WorkstationService {
    SuccessResponse createWorkstation(@Valid WorkstationDTO workstationDTO);

    SuccessResponse updateWorkstation(long id, @Valid WorkstationDTO workstationDTO);

    public SuccessResponse deleteWorkstation(long id);

    Page<WorkstationDTO> findAllWorkstations(int page, int size, String managerEmail,
                                             String search, List<String> fields);
}