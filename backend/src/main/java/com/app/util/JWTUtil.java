package com.app.util;
import java.util.Date;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import io.github.cdimascio.dotenv.Dotenv;
public class JWTUtil {

    private static final String SECRET_KEY = Dotenv.load().get("JWT_SECRET");
    private static final Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
    private static final long EXPIRATION_MS = 60 * 60 * 1000; // 1 hour

    public static String generateToken(int userId, String role) {
        return JWT.create()
                .withSubject(String.valueOf(userId))
                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .sign(algorithm);
    }

    public static int getUserId(String token) {
        return Integer.parseInt(
            JWT.require(algorithm).build().verify(token).getSubject()
        );
    }

    public static String getRole(String token) {
        return JWT.require(algorithm).build().verify(token)
                .getClaim("role").asString();
    }
    public static boolean isTokenValid(String token) {
        try {
            JWT.require(algorithm).build().verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public static String generateCode(){
        //6 Digit code
        int code = (int)(Math.random() * 900000) + 100000;
        return String.valueOf(code);
    }
}
