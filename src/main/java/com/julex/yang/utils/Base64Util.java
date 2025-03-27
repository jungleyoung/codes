package com.julex.yang.utils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Base64Util {
    private Base64Util(){}
    //加密
    public static String encryptBASE(byte[] key) {
        return (new BASE64Encoder()).encode(key);
    }

    //解密

    public static byte[] decryptBASE(String key) throws Exception {
        return (new BASE64Decoder()).decodeBuffer(key);
    }
}