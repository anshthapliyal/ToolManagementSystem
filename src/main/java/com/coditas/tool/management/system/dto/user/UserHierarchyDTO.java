package com.coditas.tool.management.system.dto.user;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserHierarchyDTO {
    private String email;
    private String name;
    private String role;

    private String facilityName;
    private String workplaceName;
    private String toolCribName;
    private String workstationCode;

    private List<String> workplaces; //for FacilityManager
}
