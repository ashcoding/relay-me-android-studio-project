package com.codolutions.android.common.serverside;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ServerSideSecurityUtil {
    private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String ENCRYPTION_ALGORITHM = "AES";

    private static String hashPasswordForEncryption(String salt, String password) throws Exception {
        String base64Salt = new String(Base64.encode(salt.getBytes(), Base64.NO_WRAP));
        return ServerSideSecurityUtil.hmacSha1(password, base64Salt);
    }

    private static String getEncryptionKey(String salt, String password) throws Exception {
        return hashPasswordForEncryption(salt, password).substring(0, 16);
    }

    private static String getIV(String salt, String password) throws Exception {
        return new StringBuilder(hashPasswordForEncryption(salt, password)).reverse().toString().substring(0, 16);
    }

    private static String decryptString(String cipherText, byte[] key, byte[] initialVector) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        SecretKeySpec secretKeySpecy = new SecretKeySpec(key, ENCRYPTION_ALGORITHM);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(initialVector);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpecy, ivParameterSpec);
        return new String(cipher.doFinal(Base64.decode(cipherText, Base64.NO_WRAP)));
    }

    private static String hmacSha1(String value, String key) throws UnsupportedEncodingException,
            NoSuchAlgorithmException, InvalidKeyException {
        String type = "HmacSHA1";
        SecretKeySpec secret = new SecretKeySpec(key.getBytes(), type);
        Mac mac = Mac.getInstance(type);
        mac.init(secret);
        byte[] bytes = mac.doFinal(value.getBytes());
        return new String(Base64.encode(bytes, Base64.NO_WRAP));
    }

    // FIXME: TECHDEBT: No blanket exception
    public static String decryptString(String cipherText, String password, String salt) throws Exception {
        String key = getEncryptionKey(salt, password);
        String iv = getIV(salt, password);
        // System.out.println("Encryption Key: " + key + ", IV: " + iv);
        return decryptString(cipherText, key.getBytes(), iv.getBytes());
    }
}
