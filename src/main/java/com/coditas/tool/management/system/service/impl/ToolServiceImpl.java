package com.coditas.tool.management.system.service.impl;

import com.coditas.tool.management.system.constant.ToolCategory;
import com.coditas.tool.management.system.dto.sharedResponse.SuccessResponse;
import com.coditas.tool.management.system.dto.tool.ToolDTO;
import com.coditas.tool.management.system.entity.Tool;
import com.coditas.tool.management.system.exception.PremiseNotFoundException;
import com.coditas.tool.management.system.repository.ToolRepository;
import com.coditas.tool.management.system.service.S3Service;
import com.coditas.tool.management.system.service.ToolService;
import com.coditas.tool.management.system.specification.ToolSpecification;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ToolServiceImpl implements ToolService {

    private final ToolRepository toolRepository;
    private final ModelMapper modelMapper;
    private final S3Service s3Service;

    @Autowired
    public ToolServiceImpl(ToolRepository toolRepository, ModelMapper modelMapper, S3Service s3Service) {
        this.toolRepository = toolRepository;
        this.modelMapper = modelMapper;
        this.s3Service = s3Service;
    }

    @Override
    public SuccessResponse addTools(ToolDTO toolDTO, MultipartFile toolImage) {
        Tool mapped = modelMapper.map(toolDTO, Tool.class);
        mapped.setId(null);

        if (toolImage != null && !toolImage.isEmpty()) {
            String key = "tools/" + toolDTO.getName() + "/" + toolImage.getOriginalFilename();
            Map<String, String> response = s3Service.uploadPhoto(toolImage, key);
            mapped.setToolImageUrl(response.get("Link")); // Ensure key is "Link" in response
        }

        if(mapped.getIsPerishable()){
            mapped.setReturnPeriod(null);
        }

        toolRepository.save(mapped);
        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Tool saved successfully.")
                .data(mapped)
                .time(LocalDateTime.now())
                .build();
    }

    @Override
    public SuccessResponse updateTools(Long id, ToolDTO toolDTO, MultipartFile toolImage) {
        Tool existingTool = toolRepository.findById(id)
                .orElseThrow(() -> new PremiseNotFoundException("Tool Not Found."));

        // Update fields from DTO
        existingTool.setName(toolDTO.getName());
        existingTool.setPrice(toolDTO.getPrice());
        existingTool.setFineAmount(toolDTO.getFineAmount());
        existingTool.setCategory(toolDTO.getCategory());
        existingTool.setIsPerishable(toolDTO.getIsPerishable());
        existingTool.setReturnPeriod(toolDTO.getReturnPeriod());

        // Image update logic
        if (toolImage != null && !toolImage.isEmpty()) {
            String key = "tools/" + toolDTO.getName() + "/" + toolImage.getOriginalFilename();
            Map<String, String> response = s3Service.uploadPhoto(toolImage, key);
            existingTool.setToolImageUrl(response.get("Link")); // Ensure 'Link' key is correct
        }
        //else keep previous image by not changing the existing URL

        Tool saved = toolRepository.save(existingTool);

        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Tool updated successfully.")
                .data(saved)
                .time(LocalDateTime.now())
                .build();
    }


    @Override
    public SuccessResponse deleteTools(Long id) {
        Tool tool = toolRepository.findById(id).orElseThrow(() -> new PremiseNotFoundException("Tool Not Found."));
        // Deleted the tool
        toolRepository.delete(tool);

        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Tool deleted successfully.")
                .time(LocalDateTime.now())
                .build();
    }

    @Override
    public Page<ToolDTO> getTools(String name, Boolean isPerishable, List<ToolCategory> category,
                                          Double minPrice, Double maxPrice, Pageable pageable) {

        Specification<Tool> spec = ToolSpecification.filterTools(name, isPerishable, category, minPrice, maxPrice);

        Page<Tool> toolPage = toolRepository.findAll(spec, pageable);

        return toolPage.map(tool -> modelMapper.map(tool, ToolDTO.class));
    }

}