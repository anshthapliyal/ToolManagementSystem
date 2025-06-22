package com.coditas.tool.management.system.service;

import com.coditas.tool.management.system.dto.sharedResponse.SuccessResponse;
import com.coditas.tool.management.system.dto.tool.ToolCribDetailsDto;
import com.coditas.tool.management.system.dto.user.UserDTO;
import com.coditas.tool.management.system.dto.user.UserListDTO;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ToolCribService {
    SuccessResponse createToolCribManager(UserDTO dto);

    SuccessResponse updateToolCribManager(Long id, UserDTO dto) throws BadRequestException;

    SuccessResponse deleteToolCribManager(Long id) throws BadRequestException;

    Page<UserListDTO> getToolCribManagers(int page, int size, String search, List<String> fields);

    Page<ToolCribDetailsDto> getToolCribsForFacilityManager(String search,
                                                            List<String> fields, int page, int size);
}