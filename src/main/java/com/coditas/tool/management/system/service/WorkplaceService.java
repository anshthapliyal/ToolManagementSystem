package com.coditas.tool.management.system.service;

import com.coditas.tool.management.system.dto.sharedResponse.SuccessResponse;
import com.coditas.tool.management.system.dto.premises.WorkplaceDTO;
import com.coditas.tool.management.system.dto.premises.WorkplaceListDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WorkplaceService {

    public SuccessResponse createWorkplace(WorkplaceDTO workplaceDTO);

    SuccessResponse updateWorkplace(long id, WorkplaceDTO workplaceDTO);

    SuccessResponse deleteWorkplace(long id);

    Page<WorkplaceListDTO> findAllWorkplaces
            (int page, int size, String search, List<String> fields, String email);

    Page<WorkplaceListDTO> getWorkplacesByFacility(Long facilityId, int page, int size, String search, List<String> fields);
}
