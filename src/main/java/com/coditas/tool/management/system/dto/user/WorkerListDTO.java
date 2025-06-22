package com.coditas.tool.management.system.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerListDTO {
    private Long id;
    private String name;
    private String email;
    private String workstationCode;
    private String workplaceName;
    private String facilityName;
}
