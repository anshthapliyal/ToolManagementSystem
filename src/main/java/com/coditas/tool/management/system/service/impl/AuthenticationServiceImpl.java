package com.coditas.tool.management.system.service.impl;

import com.coditas.tool.management.system.dto.auth.JwtResponse;
import com.coditas.tool.management.system.exception.UnauthorizedException;
import com.coditas.tool.management.system.security.JwtHelper;
import com.coditas.tool.management.system.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private AuthenticationManager manager;
    private JwtHelper helper;
    private UserDetailsService userDetailsService;

    @Autowired
    public AuthenticationServiceImpl(AuthenticationManager manager, JwtHelper helper,
                                     UserDetailsService userDetailsService) {
        this.manager = manager;
        this.helper = helper;
        this.userDetailsService = userDetailsService;
    }

    public JwtResponse doAuthenticate(String email, String password) {

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, password);
        try {
            manager.authenticate(authentication);
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Invalid Email or Password !!");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        String role = roles.get(0).toLowerCase().substring(5);

        String token = helper.generateToken(userDetails.getUsername(), roles);

        JwtResponse response = JwtResponse.builder()
                .jwtToken(token)
                .role(role)
                .build();

        return response;
    }
}
