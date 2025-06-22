package com.coditas.tool.management.system.service;

import com.coditas.tool.management.system.dto.premises.FacilityDTO;
import com.coditas.tool.management.system.dto.premises.FacilityListDTO;
import com.coditas.tool.management.system.dto.sharedResponse.SuccessResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface FacilityService {
    public SuccessResponse createFacility(FacilityDTO dto);
    public SuccessResponse updateFacility(long id, FacilityDTO dto);
    public Page<FacilityListDTO> findAllFacilities(int page,
                                                   int size, String search, List<String> fields);
    public SuccessResponse deleteFacility(long id);
}