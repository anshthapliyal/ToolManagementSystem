package com.coditas.tool.management.system.dto.auth;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {

    private String role;
    private String jwtToken;
}
