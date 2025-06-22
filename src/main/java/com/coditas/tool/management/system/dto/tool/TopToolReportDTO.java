package com.coditas.tool.management.system.dto.tool;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TopToolReportDTO {
    private String toolName;
    private Long value; //demand
}