package com.example.diverscan.activeid.Utilities;

import android.util.Base64;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class EncryptUtil {

    private static final String SEC_KEY = "password drowssap";

    public static String decrypting(String cipherString, boolean useHashing) {
        try {
            byte[] array = Base64.decode(cipherString, Base64.DEFAULT);
            byte[] key;

            if (useHashing) {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                key = md5.digest(SEC_KEY.getBytes("UTF-8"));
            } else {
                key = SEC_KEY.getBytes("UTF-8");
            }

            SecretKeySpec secretKey = new SecretKeySpec(key, "DESede");
            Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(array);

            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String encrypting(String plainText, boolean useHashing) {
        try {
            byte[] input = plainText.getBytes("UTF-8");
            byte[] key;

            if (useHashing) {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                key = md5.digest(SEC_KEY.getBytes("UTF-8"));
            } else {
                key = SEC_KEY.getBytes("UTF-8");
            }

            SecretKeySpec secretKey = new SecretKeySpec(key, "DESede");
            Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS7Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(input);

            return Base64.encodeToString(encrypted, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

