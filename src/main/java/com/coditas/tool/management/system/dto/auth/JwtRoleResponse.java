package com.coditas.tool.management.system.dto.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class JwtRoleResponse {
    private String role;
}
