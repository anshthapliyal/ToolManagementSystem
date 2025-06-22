package com.coditas.tool.management.system.dto.premises;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class FacilityListDTO {
    private Long id;
    private String name;
    private String address;
    private LocalDateTime createdAt;
    private String facilityManagerName;
    private String facilityManagerEmail;
    private List<WorkplaceDTO> workplaceDTOList;
}