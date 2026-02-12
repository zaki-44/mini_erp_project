package com.app.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

public class PasswordUtils {

    // 1. GENERATE SALT
    // This creates a random string to add to the password so identical passwords look different in the DB.
    public static String getSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    // 2. HASH PASSWORD
    // This mixes the password + salt and scrambles them using the PBKDF2 algorithm.
    public static String hashPassword(String password, String salt) {
        try {
            // Configuration: 65536 iterations (makes it slow for hackers), 128-bit length
            KeySpec spec = new PBEKeySpec(password.toCharArray(), Base64.getDecoder().decode(salt), 65536, 128);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    // 3. VERIFY PASSWORD
    // Used during Login. It takes the password the user just typed, adds the OLD salt from the DB,
    // and checks if the result matches the OLD hash from the DB.
    public static boolean verifyPassword(String providedPassword, String storedHash, String storedSalt) {
        String newHash = hashPassword(providedPassword, storedSalt);
        return newHash.equals(storedHash);
    }
    
    // TEST IT RIGHT NOW
    // You can right-click this file and choose "Run Java" to see if it works.
    public static void main(String[] args) {
        // A. Simulate Registration
        String myPassword = "secretPassword123";
        String salt = getSalt();
        String hash = hashPassword(myPassword, salt);
        
        System.out.println("--- Registration ---");
        System.out.println("Original: " + myPassword);
        System.out.println("Salt: " + salt);
        System.out.println("Hash: " + hash);
        
        // B. Simulate Login (Success)
        boolean isSuccess = verifyPassword("secretPassword123", hash, salt);
        System.out.println("Login with 'secretPassword123': " + (isSuccess ? "SUCCESS" : "FAIL"));
        
        // C. Simulate Login (Failure)
        boolean isFail = verifyPassword("wrongPassword", hash, salt);
        System.out.println("Login with 'wrongPassword': " + (isFail ? "SUCCESS" : "FAIL"));
    }
}