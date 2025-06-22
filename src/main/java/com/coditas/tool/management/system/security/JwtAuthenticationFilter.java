package com.coditas.tool.management.system.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private Logger logger = LoggerFactory.getLogger(OncePerRequestFilter.class);
    private JwtHelper jwtHelper;
    private UserDetailsService userDetailsService;

    @Autowired
    public JwtAuthenticationFilter(JwtHelper jwtHelper, UserDetailsService userDetailsService) {
        this.jwtHelper = jwtHelper;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        // Skip filter for public endpoints
        if (path.startsWith("/auth/register") ||
                path.startsWith("/auth/login")||
                path.startsWith("/swagger-ui/**") ||
                path.startsWith("/swagger-ui.html")) {
            filterChain.doFilter(request, response);
            return;
        }

        //Initializing the values
        String token = null;
        String username = null;

        try {
            //Try to fetch the request header if it exists. If found is assigned otherwise set to null
            String requestHeader = request.getHeader("Authorization");
            if (requestHeader != null && requestHeader.startsWith("Bearer")) {
                //Comes into the condition iff the request header is a bearer token and a header exists
                token = requestHeader.substring(7);
                if(jwtHelper.validateToken(token)) {
                    //Comes into this condition only if the token is validated
                    username = jwtHelper.getUsernameFromToken(token);
                    // get the Roles from the token
                    List<String> roles = jwtHelper.getRolesFromToken(token);

                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList();


                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    //Fetch details of the user such as roles from the filter chain or the db
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

                    //Authentication is set by sending the username and authorities using a UsernamePasswordAuthenticationToken
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); // This is used in case of Ip address and session id logging can be avoided
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    //Sets the current threads context authentication to the user whose token is already validated
                }else{
                    logger.warn("Validation failed : JWT token is invalid");
                    //Control comes here if the validation fails
                }
            } else {
                //Control comes here if the authorization header is missing ot bearer is missing
            logger.info("Authorization header is missing or does not start with Bearer.");
            }
            } catch (Exception e) {
            System.out.println("Cannot set user authentication: " + e);
        }
        filterChain.doFilter(request,response);
        }

}