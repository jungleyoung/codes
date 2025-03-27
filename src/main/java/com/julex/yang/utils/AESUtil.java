package com.julex.yang.utils;

import com.alibaba.fastjson.JSONObject;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class AESUtil {
    private AESUtil(){}

    // ################## AES自定义密钥 start ######################
    private static final String AESKEY = "0123456789abcdef"; //AES加密参数（必须16位）
    private static final String AES_IVPARAMETER = "0123456789abcdef"; //AES加密向量（必须16位）
    private static final String UTF8="utf-8";

    // AES加密
    public static String encrypt(String sSrc) {
        try{
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] raw = AESKEY.getBytes("ASCII");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            IvParameterSpec iv = new IvParameterSpec(AES_IVPARAMETER.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(sSrc.getBytes(UTF8));
            return Base64Util.encryptBASE(encrypted);
        } catch (Exception ex) {
            return null;
        }
    }

    // AES解密
    public static String decrypt(String sSrc)  {
        try {
            byte[] raw = AESKEY.getBytes("ASCII");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(AES_IVPARAMETER.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] encrypted = Base64Util.decryptBASE(sSrc);
            byte[] original = cipher.doFinal(encrypted);
            return new String(original, UTF8);
        } catch (Exception ex) {
            return null;
        }
    }
    // ################## AES自定义密钥 end ######################


    // ################## AES自动生成密钥 start ######################
    //产生密钥
    public static String initAESkey() throws Exception {

        KeyGenerator keyGen = KeyGenerator.getInstance("AES");

        SecretKey secretKey = keyGen.generateKey();


        return Base64Util.encryptBASE(secretKey.getEncoded());
    }

    /**
     * 兼容Linux
     * @param sKey
     * @param cipherMode
     * @return
     * @throws Exception
     */
    public static Cipher initAESCipher(String sKey, int cipherMode) throws Exception{

        // 创建Key gen
        KeyGenerator keyGenerator = null;
        Cipher cipher = null;

        try {
            keyGenerator = KeyGenerator.getInstance("AES");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(sKey.getBytes());
            keyGenerator.init(128, random);
            SecretKey secretKey = keyGenerator.generateKey();
            byte[] codeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(codeFormat, "AES");
            cipher = Cipher.getInstance("AES");
            // 初始化
            cipher.init(cipherMode, key);
        } catch (Exception e) {
            throw e;
        }
        return cipher;
    }

    /**
     * 对文件进行AES加密
     *
     * @param sourceFile
     * @param encrypfile
     * @param sKey
     * @return
     */
    public static File encryptFile(File sourceFile, File encrypfile, String sKey) throws Exception {

        InputStream inputStream = null;
        OutputStream outputStream = null;
        CipherInputStream cipherInputStream = null;
        Exception ex = null;
        try {
            inputStream = new FileInputStream(sourceFile);

            outputStream = new FileOutputStream(encrypfile);
            Cipher cipher = initAESCipher(sKey, Cipher.ENCRYPT_MODE);
            // 以加密流写入文件
            cipherInputStream = new CipherInputStream(inputStream, cipher);
            byte[] cache = new byte[1024];
            int nRead = 0;
            while ((nRead = cipherInputStream.read(cache)) != -1) {
                outputStream.write(cache, 0, nRead);
                outputStream.flush();
            }
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (cipherInputStream!=null) {
                    cipherInputStream.close();
                }
            } catch (Exception e) {
                ex = e;
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                ex = e;
            }
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                ex = e;
            }
        }
        if (ex != null) {
            throw ex;
        }
        return encrypfile;
    }

    /**
     *  AES方式解密文件
     * @param sourceFile 要解密的文件路径
     * @param decryptFile 解密后的文件路径
     * @param sKey
     * @return
     */
    public static File decryptFile(File sourceFile, File decryptFile, String sKey) throws Exception {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        CipherOutputStream cipherOutputStream = null;
        Exception ex = null;
        try {
            Cipher cipher = initAESCipher(sKey, Cipher.DECRYPT_MODE);
            inputStream = new FileInputStream(sourceFile);
            outputStream = new FileOutputStream(decryptFile);
            cipherOutputStream = new CipherOutputStream(outputStream, cipher);
            byte[] buffer = new byte[1024];
            int r;
            while ((r = inputStream.read(buffer)) >= 0) {
                cipherOutputStream.write(buffer, 0, r);
            }
            cipherOutputStream.close();
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (cipherOutputStream != null) {
                    cipherOutputStream.close();
                }
            } catch (Exception e) {
                ex = e;
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                ex = e;
            }
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                ex = e;
            }

        }
        if (ex != null) {
            throw ex;
        }
        return decryptFile;
    }

    //加密
    public static String encrypt(String data, String key) throws Exception {
        SecretKey secretKey = new SecretKeySpec(Base64Util.decryptBASE(key), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(data.getBytes(UTF8));
        return Base64Util.encryptBASE(encrypted);
    }

    //解密
    public static String decrypt(String data, String key) throws Exception {
        SecretKey secretKey = new SecretKeySpec(Base64Util.decryptBASE(key), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] encrypted = Base64Util.decryptBASE(data);
        byte[] original = cipher.doFinal(encrypted);
        return new String(original, UTF8);
    }
    // ################## AES自动生成密钥 end ######################

    public static void main(String[] args) throws Exception {
        newToken();
    }

    public static void newToken(){

    }

}
