package de.finnik.passvault.AES;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AES {

    private final Map<String, SecretKeySpec> secretKeySpec;

    public AES(String pass) {
        secretKeySpec = new HashMap<>();
        for(String encryption:new String[]{"SHA-256", "SHA-1"}) {
            secretKeySpec.put(encryption, getSecretKey(pass, encryption));
        }
    }

    private static SecretKeySpec getSecretKey(String myKey, String encryption) {
        MessageDigest sha;
        byte[] key;
        try {
            key = myKey.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance(encryption);
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            return new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error while creating Secret key: "+e.getMessage());
        }
    }

    /**Encrypts a string via a given key with Advanced Encrpytion Standard (AES).
     * @param strToEncrypt String to encrypt
     * @return Encrypted key
     */
    public String encrypt(String strToEncrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec.get("SHA-256"));
                return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("Error while encrypting: " + e.toString());
        }
    }

    /**
     * Decrypts a string with a given key via Advanced Encryption Standard (AES).
     *
     * @param strToDecrypt String to decrypt
     * @return Decrypted key
     * @throws WrongPasswordException Wrong password!
     */
    public String decrypt(String strToDecrypt) throws WrongPasswordException {
        try {
            return decrypt(strToDecrypt, "SHA-256");
        } catch (Exception e) {
            return decrypt(strToDecrypt, "SHA-1"); 
        }
    }

    /**
     * Decrypts a string with a given key via Advanced Encryption Standard (AES).
     *
     * @param strToDecrypt String to decrypt
     * @return Decrypted key
     * @throws WrongPasswordException Wrong password!
     */
    private String decrypt(String strToDecrypt, String encryption) throws WrongPasswordException {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec.get(encryption));
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt.getBytes(StandardCharsets.UTF_8))));
        } catch (Exception e) {
            throw new WrongPasswordException();
        }
    }

    public static class WrongPasswordException extends RuntimeException {

    }
}
