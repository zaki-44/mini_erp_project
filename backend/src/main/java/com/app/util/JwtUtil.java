package com.app.util;

import java.util.Date;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.app.model.User; 

public class JwtUtil {

    private static final String SECRET_KEY = "super_secret_key_must_be_very_long_and_secure_for_production";
    private static final Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
    private static final long EXPIRATION_MS = 60 * 60 * 1000 * 10; // 10 hours

    // 1. GENERATE TOKEN (For LoginServlet)
    public static String generateToken(User user) {
        return JWT.create()
                .withSubject(String.valueOf(user.getId()))
                .withClaim("role", user.getRole())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .sign(algorithm);
    }

    // 2. VALIDATE TOKEN (For JwtFilter)
    public static DecodedJWT validateToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).build();
            return verifier.verify(token); 
        } catch (Exception e) {
            return null; 
        }
    }

    // 3. GET ROLE (For JwtFilter)
    public static String getRole(DecodedJWT jwt) {
        return jwt.getClaim("role").asString();
    }
    
    // 4. GET ID (For JwtFilter)
    public static int getUserId(DecodedJWT jwt) {
        return Integer.parseInt(jwt.getSubject());
    }

    // 5. GENERATE VERIFICATION CODE (For RegisterServlet)
    // I added this back!
    public static String generateCode(){
        int code = (int)(Math.random() * 900000) + 100000;
        return String.valueOf(code);
    }
}