package com.bank.config.client.hotupdate;

import com.bank.config.client.cache.ConfigCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置热更新处理器
 * 用于处理@ConfigValue注解标记的字段和方法
 * 
 * @author bank
 */
public class ConfigHotUpdateProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigHotUpdateProcessor.class);
    
    private final ConfigHotUpdateManager hotUpdateManager;
    private final ConfigCache configCache;
    private final ConcurrentHashMap<Object, Boolean> processedObjects = new ConcurrentHashMap<>();
    
    public ConfigHotUpdateProcessor(ConfigHotUpdateManager hotUpdateManager, ConfigCache configCache) {
        this.hotUpdateManager = hotUpdateManager;
        this.configCache = configCache;
    }
    
    /**
     * 处理对象的配置热更新
     * 
     * @param target 目标对象
     */
    public void processObject(Object target) {
        if (target == null || processedObjects.containsKey(target)) {
            return;
        }
        
        try {
            processFields(target);
            processMethods(target);
            processedObjects.put(target, true);
            
            logger.debug("处理对象配置热更新完成: {}", target.getClass().getSimpleName());
        } catch (Exception e) {
            logger.error("处理对象配置热更新失败: {}", target.getClass().getSimpleName(), e);
        }
    }
    
    /**
     * 处理字段的配置热更新
     */
    private void processFields(Object target) {
        Class<?> clazz = target.getClass();
        
        // 处理当前类的字段
        processClassFields(clazz, target);
        
        // 处理父类的字段
        Class<?> superClass = clazz.getSuperclass();
        while (superClass != null && superClass != Object.class) {
            processClassFields(superClass, target);
            superClass = superClass.getSuperclass();
        }
    }
    
    /**
     * 处理指定类的字段
     */
    private void processClassFields(Class<?> clazz, Object target) {
        Field[] fields = clazz.getDeclaredFields();
        
        for (Field field : fields) {
            ConfigValue configValue = field.getAnnotation(ConfigValue.class);
            if (configValue != null) {
                processConfigValueField(target, field, configValue);
            }
        }
    }
    
    /**
     * 处理配置值字段
     */
    private void processConfigValueField(Object target, Field field, ConfigValue configValue) {
        try {
            field.setAccessible(true);
            
            // 确定配置键
            String configKey = determineConfigKey(field, configValue);
            
            // 获取配置值
            String configValueStr = getConfigValue(configKey, configValue);
            
            // 设置字段值
            if (configValueStr != null) {
                Object convertedValue = convertValue(configValueStr, field.getType());
                field.set(target, convertedValue);
                
                logger.debug("设置配置字段: {}.{} = {} (配置键: {})", 
                    target.getClass().getSimpleName(), 
                    field.getName(), 
                    configValueStr, 
                    configKey);
            }
            
            // 绑定到热更新管理器
            hotUpdateManager.bindConfigField(configKey, target, field.getName());
            
        } catch (Exception e) {
            logger.error("处理配置值字段失败: {}.{}", 
                target.getClass().getSimpleName(), 
                field.getName(), e);
        }
    }
    
    /**
     * 处理方法的配置热更新
     */
    private void processMethods(Object target) {
        Class<?> clazz = target.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        
        for (Method method : methods) {
            ConfigValue configValue = method.getAnnotation(ConfigValue.class);
            if (configValue != null) {
                processConfigValueMethod(target, method, configValue);
            }
        }
    }
    
    /**
     * 处理配置值方法
     */
    private void processConfigValueMethod(Object target, Method method, ConfigValue configValue) {
        try {
            // 确定配置键
            String configKey = determineConfigKey(method, configValue);
            
            // 获取配置值
            String configValueStr = getConfigValue(configKey, configValue);
            
            // 调用方法设置值
            if (configValueStr != null) {
                Class<?>[] paramTypes = method.getParameterTypes();
                Object[] args = new Object[paramTypes.length];
                
                for (int i = 0; i < paramTypes.length; i++) {
                    args[i] = convertValue(configValueStr, paramTypes[i]);
                }
                
                method.invoke(target, args);
                
                logger.debug("调用配置方法: {}.{}() (配置键: {})", 
                    target.getClass().getSimpleName(), 
                    method.getName(), 
                    configKey);
            }
            
            // 绑定到热更新管理器
            hotUpdateManager.bindConfigMethod(configKey, target, method.getName(), method.getParameterTypes());
            
        } catch (Exception e) {
            logger.error("处理配置值方法失败: {}.{}()", 
                target.getClass().getSimpleName(), 
                method.getName(), e);
        }
    }
    
    /**
     * 确定配置键
     */
    private String determineConfigKey(Field field, ConfigValue configValue) {
        String key = configValue.value();
        if (key.isEmpty()) {
            key = field.getName();
        }
        
        String prefix = configValue.prefix();
        if (!prefix.isEmpty()) {
            if (prefix.endsWith(".")) {
                key = prefix + key;
            } else {
                key = prefix + "." + key;
            }
        }
        
        return key;
    }
    
    /**
     * 确定配置键（方法版本）
     */
    private String determineConfigKey(Method method, ConfigValue configValue) {
        String key = configValue.value();
        if (key.isEmpty()) {
            key = method.getName();
        }
        
        String prefix = configValue.prefix();
        if (!prefix.isEmpty()) {
            if (prefix.endsWith(".")) {
                key = prefix + key;
            } else {
                key = prefix + "." + key;
            }
        }
        
        return key;
    }
    
    /**
     * 获取配置值
     */
    private String getConfigValue(String configKey, ConfigValue configValue) {
        // 首先尝试从缓存获取
        String value = configCache.get(configKey);
        
        if (value != null) {
            return value;
        }
        
        // 如果缓存中没有，使用默认值
        String defaultValue = configValue.defaultValue();
        if (!defaultValue.isEmpty()) {
            return defaultValue;
        }
        
        // 如果配置是必填的，抛出异常
        if (configValue.required()) {
            throw new RuntimeException("必填配置项不存在: " + configKey);
        }
        
        return null;
    }
    
    /**
     * 转换配置值类型
     */
    private Object convertValue(String value, Class<?> targetType) {
        if (targetType == String.class) {
            return value;
        } else if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(value);
        } else if (targetType == Long.class || targetType == long.class) {
            return Long.parseLong(value);
        } else if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(value);
        } else if (targetType == Float.class || targetType == float.class) {
            return Float.parseFloat(value);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(value);
        } else {
            // 对于其他类型，尝试使用默认构造函数或返回字符串
            return value;
        }
    }
    
    /**
     * 移除已处理的对象
     */
    public void removeProcessedObject(Object target) {
        processedObjects.remove(target);
    }
    
    /**
     * 清除所有已处理的对象
     */
    public void clearProcessedObjects() {
        processedObjects.clear();
    }
}
