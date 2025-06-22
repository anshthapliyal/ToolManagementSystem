package com.coditas.tool.management.system.service;

import com.coditas.tool.management.system.constant.ToolCategory;
import com.coditas.tool.management.system.dto.sharedResponse.SuccessResponse;
import com.coditas.tool.management.system.dto.tool.ToolDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ToolService {
    SuccessResponse addTools(ToolDTO toolDTO, MultipartFile toolImage);
    SuccessResponse updateTools(Long id, ToolDTO toolDTO, MultipartFile toolImage);

    SuccessResponse deleteTools(Long id);


    Page<ToolDTO> getTools(String name, Boolean isPerishable, List<ToolCategory> categories,
                           Double minPrice, Double maxPrice, Pageable pageable);

}