package com.coditas.tool.management.system.service.impl;

import com.coditas.tool.management.system.dto.sharedResponse.SuccessResponse;
import com.coditas.tool.management.system.dto.tool.TopToolReportDTO;
import com.coditas.tool.management.system.repository.ToolInventoryRepository;
import com.coditas.tool.management.system.repository.ToolRepository;
import com.coditas.tool.management.system.repository.ToolRequestItemRepository;
import com.coditas.tool.management.system.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    private final ToolRequestItemRepository toolRequestItemRepository;
    private final ToolInventoryRepository toolInventoryRepository;
    private final ToolRepository toolRepository;

    @Autowired
    public ReportServiceImpl(ToolRequestItemRepository toolRequestItemRepository, ToolInventoryRepository toolInventoryRepository, ToolRepository toolRepository) {
        this.toolRequestItemRepository = toolRequestItemRepository;
        this.toolInventoryRepository = toolInventoryRepository;
        this.toolRepository = toolRepository;
    }

    @Override
    public SuccessResponse getTopDemandedTools() {
        List<TopToolReportDTO> rawList = toolRequestItemRepository.findTopDemandedTools();

        List<TopToolReportDTO> top3 = new ArrayList<>();
        for (int i = 0; i < 3; i++) { //top 3 will be looped. If any item has null/0 value, it will be linked as null
            if (i < rawList.size()) {
                top3.add(rawList.get(i));
            } else {
                top3.add(new TopToolReportDTO(null, null));
            }
        }
        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Here are the top 3 demanded tools !")
                .data(top3)
                .time(LocalDateTime.now())
                .build();
    }

    @Override
    public SuccessResponse getMostBrokenTools() {
        List<TopToolReportDTO> rawList = toolInventoryRepository.findTopBrokenTools();

        List<TopToolReportDTO> top3 = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            if (i < rawList.size()) {
                top3.add(rawList.get(i));
            } else {
                top3.add(TopToolReportDTO.builder().toolName(null).value(null).build());
            }
        }

        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Here are the top 3 most broken tools.")
                .data(top3)
                .time(LocalDateTime.now())
                .build();
    }

    @Override
    public SuccessResponse getTopPricedTools() {
        List<TopToolReportDTO> rawList = toolRepository.findTopPricedTools();

        List<TopToolReportDTO> top3 = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            if (i < rawList.size()) {
                top3.add(rawList.get(i));
            } else {
                top3.add(TopToolReportDTO.builder().toolName(null).value(null).build());
            }
        }

        return SuccessResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Here are the top 3 priced tools!")
                .data(top3)
                .time(LocalDateTime.now())
                .build();
    }
}