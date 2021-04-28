/***
 * This class is used to encrypt a password using a 128-bit key.
 * Sergey Kargopolov's example was used as a template for this class.
 * Find his blog post at: https://www.appsdeveloperblog.com/encrypt-user-password-example-java/
 */


package com.example.main;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

public class EncryptPassword {
    private static final Random RANDOM = new SecureRandom();
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static String salt(int length){
        StringBuilder returnValue = new StringBuilder(length);

        for(int i = 0; i < length; i++){
            returnValue.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }

        return new String(returnValue);
    }

    public static byte[] hashPassword(char[] password, byte[] salt){
        PBEKeySpec keySpec = new PBEKeySpec(password, salt, 5000, 128);
        Arrays.fill(password, Character.MIN_VALUE);
        try{
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return secretKeyFactory.generateSecret(keySpec).getEncoded();
        }catch(NoSuchAlgorithmException | InvalidKeySpecException e){
            throw new AssertionError("Error while hashing password: " + e.getMessage(), e);
        }finally{
            keySpec.clearPassword();
        }
    }

    public static String generatePassword(String plainPassword, String salt){
        byte[] encryptedPassword = hashPassword(plainPassword.toCharArray(), salt.getBytes());

        return Base64.getEncoder().encodeToString(encryptedPassword);
    }

    public static boolean checkPassword(String plainPassword, String encryptedPassword, String salt){
        String newEncryptedPassword = generatePassword(plainPassword, salt);
        return newEncryptedPassword.equalsIgnoreCase(encryptedPassword);
    }
}
