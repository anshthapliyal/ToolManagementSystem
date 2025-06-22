package com.coditas.tool.management.system.dto.premises;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkplaceListDTO {
    private Long id;
    private String name;
    private String workplaceManagerName;
    private String workplaceManagerEmail;
    private String facilityName;
    private LocalDateTime createdAt;
}
