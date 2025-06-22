package com.coditas.tool.management.system.dto.user;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserListDTO {
    private Long id;
    private String name;
    private String email;
    private LocalDateTime createdAt;
    private String facilityName;
    private String facilityAddress;
}