package com.coditas.tool.management.system.security;

import com.coditas.tool.management.system.exception.UnauthorizedException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtHelper {
    @Value("${jwt.token.validity}")
    public long jwtTokenValidity;

    @Value("${jwt.token.secret}")
    public String secret;

    private SecretKey key;
    private Logger logger = LoggerFactory.getLogger(JwtHelper.class);

    @PostConstruct
    public void init(){
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        // This method is used to generate a key from the provided secret. The given secret is checked for length
        // and then converted into bytes suitable for the crypto algorithms. The key contains the same info as the secret
        // but in binary format
    }

    //Generate JWT Token
    public String generateToken(String username, List<String> roles) {
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("roles", roles);

            return Jwts.builder()   // Returns a jwt builder that can be used to create JWt builder object
                    .setSubject(username)  // sets the "sub" claim to the username
                    .claim("roles", roles)  // Add roles as a single claim
                    .setIssuedAt(new Date())  // sets the "iat" claim to the instant
                    .setExpiration(new Date((new Date()).getTime() + jwtTokenValidity))  // set "exp" claim
                    .signWith(key, SignatureAlgorithm.HS512)  // Signs token with the key provided
                    .compact(); // Arranges in header.payload.signature format
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException(e.getMessage());
        }
    }

    public String getUsernameFromToken(String token){
        return Jwts.parserBuilder()  // returns JWT parser
                .setSigningKey(key).build() //Tells parser that we used this key and to use that for verification
                .parseClaimsJws(token)   //After build, parses the passed token
                .getBody()   // returns all the claims
                .getSubject(); //returns the username
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public List<String> getRolesFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        Object roles = claims.get("roles");
        if (roles instanceof List<?> roleList) {
            return roleList.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public Boolean validateToken(String token){
        try{
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            // parserBuilder - starts the jwt parser
            // setSigningKey - specifies the key used to sign the jwt
            // build - builds the parser
            // parseClaimsJws - Validation step, token is split into header and payload,
            // decodes them using base 64 encoder, recomputes signature using header+payload
            // compares the generated token to the existing token and after all this succeeds true is thrown
            return true;
        }catch (SecurityException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
            throw new UnauthorizedException("Invalid JWT signature: " + e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Malformed JWT token: {}", e.getMessage());
            throw new UnauthorizedException("Malformed JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
            throw new UnauthorizedException("JWT token is expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
            throw new UnauthorizedException("JWT token is unsupported: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
            throw new IllegalArgumentException("JWT claims string is empty: " + e.getMessage());
        }
        //return false;
    }
}
