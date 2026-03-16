package com.horzits.common.utils;

import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import cn.hutool.core.util.CharsetUtil;

/**
 * SM4 加密工具类
 */
public class Sm4Utils {
    // 128位密钥 (16字节)，生产环境请配置在 yml 或环境变量中，不要硬编码！
    private static final String KEY = "1234567890123456"; 
    private static final SymmetricCrypto sm4 = SmUtil.sm4(KEY.getBytes());

    public static String encrypt(String content) {
        if (content == null || content.isEmpty()) return content;
        try {
             return sm4.encryptHex(content); // 加密为 Hex 字符串存储
        } catch (Exception e) {
             e.printStackTrace();
             return content;
        }
    }

    public static String decrypt(String encryptHex) {
        if (encryptHex == null || encryptHex.isEmpty()) return encryptHex;
        try {
            return sm4.decryptStr(encryptHex, CharsetUtil.CHARSET_UTF_8);
        } catch (Exception e) {
            // 解密失败（可能是历史明文数据），直接返回原值
            return encryptHex;
        }
    }
}
