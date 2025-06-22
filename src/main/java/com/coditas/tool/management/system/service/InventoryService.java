package com.coditas.tool.management.system.service;

import com.coditas.tool.management.system.constant.ToolCategory;
import com.coditas.tool.management.system.dto.tool.ToolInventoryDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InventoryService {

    Page<ToolInventoryDTO> getInventoryForManager(String email, String name, Boolean isPerishable,
                                                  List<ToolCategory> category, Double minPrice,
                                                  Double maxPrice, int page, int size);

    Page<ToolInventoryDTO> getInventoryForWorker(String email, String name, Boolean isPerishable,
                                                 List<ToolCategory> category, Double minPrice,
                                                 Double maxPrice, int page, int size);

    Page<ToolInventoryDTO> getInventoryForToolCribManager(String email, String name, List<ToolCategory> category,
                                                          Boolean isPerishable, Double minPrice, Double maxPrice,
                                                          int page, int size);
}
