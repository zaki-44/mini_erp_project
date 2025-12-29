package com.app.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import com.app.model.User;

import java.security.Key;
import java.util.Date;


public class JwtUtil {

    // 1. The Secret Key (Keep this SAFE! Use .env in production)
    // It must be at least 256 bits (32 characters)
    private static final String SECRET_KEY = "super_secret_key_must_be_very_long_and_secure_for_production";
    
    private static final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    
    // Token valid for 10 Hours
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 10; 

    // GENERATE TOKEN
    public static String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail()) // Main identifier
                .claim("id", user.getId())
                .claim("role", user.getRole()) // Store role inside token
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // VALIDATE TOKEN
    public static Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            // Token is invalid or expired
            return null;
        }
    }
}