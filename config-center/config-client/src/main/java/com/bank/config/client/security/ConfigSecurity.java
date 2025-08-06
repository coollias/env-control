package com.bank.config.client.security;

import org.apache.http.HttpRequest;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * 配置安全类
 * 提供认证和加密功能
 * 
 * @author bank
 */
public class ConfigSecurity {
    private static final Logger logger = LoggerFactory.getLogger(ConfigSecurity.class);

    private final String token;
    private final String appCode;
    private final String envCode;
    private final String encryptionKey;

    public ConfigSecurity(String token, String appCode, String envCode) {
        this.token = token;
        this.appCode = appCode;
        this.envCode = envCode;
        this.encryptionKey = generateEncryptionKey();
    }

    /**
     * 添加认证头到HTTP请求
     */
    public void addAuthHeaders(HttpRequest request) {
        if (token != null && !token.trim().isEmpty()) {
            // 如果token已经包含Bearer前缀，直接使用；否则添加Bearer前缀
            String authToken = token.trim().startsWith("Bearer ") ? token.trim() : "Bearer " + token.trim();
            request.addHeader(new BasicHeader("Authorization", authToken));
        }
        
        if (appCode != null && !appCode.trim().isEmpty()) {
            request.addHeader(new BasicHeader("X-App-Code", appCode));
        }
        
        if (envCode != null && !envCode.trim().isEmpty()) {
            request.addHeader(new BasicHeader("X-Env-Code", envCode));
        }
        
        // 添加时间戳防止重放攻击
        request.addHeader(new BasicHeader("X-Timestamp", String.valueOf(System.currentTimeMillis())));
    }

    /**
     * 加密配置值
     */
    public String encrypt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }

        try {
            SecretKeySpec secretKey = new SecretKeySpec(encryptionKey.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] encryptedBytes = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            logger.error("加密失败", e);
            return value;
        }
    }

    /**
     * 解密配置值
     */
    public String decrypt(String encryptedValue) {
        if (encryptedValue == null || encryptedValue.trim().isEmpty()) {
            return encryptedValue;
        }

        try {
            SecretKeySpec secretKey = new SecretKeySpec(encryptionKey.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedValue));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("解密失败", e);
            return encryptedValue;
        }
    }

    /**
     * 检查值是否已加密
     */
    public boolean isEncrypted(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        
        try {
            // 尝试Base64解码
            Base64.getDecoder().decode(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 生成加密密钥
     */
    private String generateEncryptionKey() {
        try {
            String keySource = (appCode != null ? appCode : "") + 
                             (envCode != null ? envCode : "") + 
                             (token != null ? token : "");
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(keySource.getBytes(StandardCharsets.UTF_8));
            
            // 取前16字节作为AES密钥
            byte[] keyBytes = new byte[16];
            System.arraycopy(hash, 0, keyBytes, 0, Math.min(hash.length, 16));
            
            return Base64.getEncoder().encodeToString(keyBytes);
        } catch (Exception e) {
            logger.error("生成加密密钥失败", e);
            // 使用默认密钥
            return "defaultKey123456";
        }
    }

    /**
     * 生成请求签名
     */
    public String generateSignature(String timestamp, String nonce) {
        try {
            String signString = appCode + envCode + timestamp + nonce;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(signString.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            logger.error("生成签名失败", e);
            return "";
        }
    }

    /**
     * 验证响应签名
     */
    public boolean verifySignature(String data, String signature) {
        try {
            String expectedSignature = generateSignature(data, "");
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            logger.error("验证签名失败", e);
            return false;
        }
    }

    // Getter方法
    public String getToken() {
        return token;
    }

    public String getAppCode() {
        return appCode;
    }

    public String getEnvCode() {
        return envCode;
    }
} 