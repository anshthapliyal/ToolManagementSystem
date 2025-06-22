package com.coditas.tool.management.system.controller;

import com.coditas.tool.management.system.dto.auth.JwtRequest;
import com.coditas.tool.management.system.dto.auth.JwtResponse;
import com.coditas.tool.management.system.dto.auth.JwtRoleResponse;
import com.coditas.tool.management.system.dto.sharedResponse.GeneralErrorResponse;
import com.coditas.tool.management.system.dto.sharedResponse.SuccessResponse;
import com.coditas.tool.management.system.security.JwtHelper;
import com.coditas.tool.management.system.service.AuthenticationService;
import com.coditas.tool.management.system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Validated
public class CommonController {

    private final JwtHelper helper;
    private final AuthenticationService authenticationService;
    private final UserService userService;

    @Autowired
    public CommonController(JwtHelper helper, AuthenticationService authenticationService, UserService userService) {
        this.helper = helper;
        this.authenticationService = authenticationService;
        this.userService = userService;
    }
    @Operation(summary = "Login any user.", description = "This endpoint is the login page for users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK. Successfully logged in. " +
                    "JWT Token is created successfully.",  content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request. " +
                    "IllegalArgumentException - JWT claims string is empty." +
                    "Or General Exception.", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = GeneralErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized. " +
                    "BadCredentialsException - Invalid Email or Password. " +
                    "SecurityException - Invalid JWT signature. " +
                    "MalformedJwtException - Malformed JWT token. " +
                    "ExpiredJwtException - JWT token is expired. " +
                    "UnsupportedJwtException - JWT token is unsupported",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = GeneralErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = GeneralErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody JwtRequest request) {
        JwtResponse response = authenticationService.doAuthenticate(request.getEmail(), request.getPassword());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Gives out role from token.", description = "This endpoint gives out the role " +
            "from any token given in Authorization Header.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK. Successfully gives out role.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request. " +
                    "IllegalArgumentException - JWT claims string is empty." +
                    "Or General Exception.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = GeneralErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized. " +
                    "SecurityException - Invalid JWT signature. " +
                    "MalformedJwtException - Malformed JWT token. " +
                    "ExpiredJwtException - JWT token is expired. " +
                    "UnsupportedJwtException - JWT token is unsupported",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = GeneralErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GeneralErrorResponse.class)))
    })
    @GetMapping("/role")
    public ResponseEntity<JwtRoleResponse> getRole(HttpServletRequest request){
        String authHeader = request.getHeader("Authorization");

        if(authHeader != null && authHeader.startsWith("Bearer ")){
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            List<String> roles = helper.getRolesFromToken(token);
            JwtRoleResponse jwtRoleResponse = JwtRoleResponse.builder()
                    .role(roles.get(0).toLowerCase().substring(5))
                    .build();
            return ResponseEntity.ok(jwtRoleResponse);
        }else {
            throw new IllegalArgumentException("Invalid Token.");
        }
    }


    @Operation(summary = "Upload the profile photo and update basic user info",
            description = "This endpoint can be used to upload the profile picture and update email, name, and password.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile and details updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Invalid Token or Input",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GeneralErrorResponse.class)))
    })
    @PatchMapping("/upload-profile")
    public ResponseEntity<SuccessResponse> uploadProfilePhoto(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "email", required = false) String newEmail,
            @RequestParam(value = "password", required = false) String password) {

        return ResponseEntity.ok(userService.uploadProfilePhoto(file, name, newEmail, password));
    }

    //For testing purposes
    @GetMapping("/hierarchy")
    public ResponseEntity<Map<String, Object>> getUserHierarchy(@RequestHeader("Authorization") String token) {
        String extractedToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        Map<String, Object> response = userService.getUserHierarchyInfo(extractedToken);
        return ResponseEntity.ok(response);
    }
}