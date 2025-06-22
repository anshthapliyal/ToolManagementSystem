package com.coditas.tool.management.system.service;

import com.coditas.tool.management.system.dto.auth.JwtResponse;

public interface AuthenticationService {
    public JwtResponse doAuthenticate(String email, String password);

}
