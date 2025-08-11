package com.bank.config.client.parser;

import java.util.*;

/**
 * 配置转换器
 * 提供配置值的类型转换功能
 * 
 * @author bank
 */
public class ConfigConverter {
    
    /**
     * 将字符串转换为指定类型
     */
    public <T> T convert(String value, Class<T> targetType) {
        if (value == null) {
            return null;
        }
        
        if (targetType == String.class) {
            @SuppressWarnings("unchecked")
            T result = (T) value;
            return result;
        } else if (targetType == Integer.class || targetType == int.class) {
            @SuppressWarnings("unchecked")
            T result = (T) Integer.valueOf(value.trim());
            return result;
        } else if (targetType == Long.class || targetType == long.class) {
            @SuppressWarnings("unchecked")
            T result = (T) Long.valueOf(value.trim());
            return result;
        } else if (targetType == Double.class || targetType == double.class) {
            @SuppressWarnings("unchecked")
            T result = (T) Double.valueOf(value.trim());
            return result;
        } else if (targetType == Float.class || targetType == float.class) {
            @SuppressWarnings("unchecked")
            T result = (T) Float.valueOf(value.trim());
            return result;
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            @SuppressWarnings("unchecked")
            T result = (T) Boolean.valueOf(value.trim());
            return result;
        } else {
            throw new IllegalArgumentException("不支持的类型转换: " + targetType.getName());
        }
    }
    
    /**
     * 将字符串转换为列表
     */
    public List<String> convertToList(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // 支持逗号分隔的字符串
        String[] parts = value.split(",");
        return Arrays.asList(parts);
    }
    
    /**
     * 将字符串转换为Map
     */
    public Map<String, String> convertToMap(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new HashMap<>();
        }
        
        Map<String, String> result = new java.util.HashMap<>();
        
        // 支持key=value格式的字符串，多个用逗号分隔
        String[] pairs = value.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                result.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        
        return result;
    }
    
    /**
     * 安全转换，失败时返回默认值
     */
    public <T> T convertSafely(String value, Class<T> targetType, T defaultValue) {
        try {
            return convert(value, targetType);
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * 检查值是否可以转换为指定类型
     */
    public boolean canConvert(String value, Class<?> targetType) {
        if (value == null) {
            return true;
        }
        
        try {
            convert(value, targetType);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
} 