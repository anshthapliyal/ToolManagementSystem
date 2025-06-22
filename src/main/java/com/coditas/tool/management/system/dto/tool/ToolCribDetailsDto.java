package com.coditas.tool.management.system.dto.tool;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolCribDetailsDto {
    private Long id;
    private String name;
    private String workplaceName;
    private Long workplaceId;
    private List<String> managerEmails;
}
