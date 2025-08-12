package com.bank.config.client.hotupdate;

import com.bank.config.client.cache.ConfigCache;
import com.bank.config.client.poller.ConfigChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 配置热更新管理器
 * 负责检测配置变更并自动更新相关的对象属性
 * 
 * @author bank
 */
public class ConfigHotUpdateManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigHotUpdateManager.class);
    
    private final ConfigCache configCache;
    private final Map<String, List<ConfigFieldBinding>> fieldBindings = new ConcurrentHashMap<>();
    private final Map<String, List<ConfigMethodBinding>> methodBindings = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;
    
    public ConfigHotUpdateManager(ConfigCache configCache) {
        this.configCache = configCache;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "config-hot-update");
            thread.setDaemon(true);
            return thread;
        });
        
        // 启动配置变更检测
        startConfigChangeDetection();
    }
    
    /**
     * 绑定配置字段到对象属性
     * 
     * @param configKey 配置键
     * @param target 目标对象
     * @param fieldName 字段名
     */
    public void bindConfigField(String configKey, Object target, String fieldName) {
        try {
            Field field = getField(target.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                
                ConfigFieldBinding binding = new ConfigFieldBinding(target, field, configKey);
                fieldBindings.computeIfAbsent(configKey, k -> new ArrayList<>()).add(binding);
                
                // 立即设置初始值
                updateFieldValue(binding);
                
                logger.debug("绑定配置字段: {} -> {}.{}", configKey, target.getClass().getSimpleName(), fieldName);
            } else {
                logger.warn("字段不存在: {}.{}", target.getClass().getSimpleName(), fieldName);
            }
        } catch (Exception e) {
            logger.error("绑定配置字段失败: {} -> {}.{}", configKey, target.getClass().getSimpleName(), fieldName, e);
        }
    }
    
    /**
     * 绑定配置到方法调用
     * 
     * @param configKey 配置键
     * @param target 目标对象
     * @param methodName 方法名
     * @param parameterTypes 参数类型
     */
    public void bindConfigMethod(String configKey, Object target, String methodName, Class<?>... parameterTypes) {
        try {
            java.lang.reflect.Method method = target.getClass().getMethod(methodName, parameterTypes);
            
            ConfigMethodBinding binding = new ConfigMethodBinding(target, method, configKey);
            methodBindings.computeIfAbsent(configKey, k -> new ArrayList<>()).add(binding);
            
            // 立即调用方法设置初始值
            updateMethodValue(binding);
            
            logger.debug("绑定配置方法: {} -> {}.{}()", configKey, target.getClass().getSimpleName(), methodName);
        } catch (Exception e) {
            logger.error("绑定配置方法失败: {} -> {}.{}()", configKey, target.getClass().getSimpleName(), methodName, e);
        }
    }
    
    /**
     * 绑定配置到Spring Bean
     * 
     * @param configKey 配置键
     * @param beanName Bean名称
     * @param fieldName 字段名
     */
    public void bindSpringBean(String configKey, String beanName, String fieldName) {
        // 这里可以通过Spring上下文获取Bean
        // 暂时记录绑定信息，等待Spring上下文可用时再处理
        logger.debug("绑定Spring Bean: {} -> {}.{}", configKey, beanName, fieldName);
    }
    
    /**
     * 启动配置变更检测
     */
    private void startConfigChangeDetection() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                detectConfigChanges();
            } catch (Exception e) {
                logger.error("配置变更检测失败", e);
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS); // 每秒检测一次
        
        logger.info("配置热更新管理器已启动");
    }
    
    /**
     * 检测配置变更
     */
    private void detectConfigChanges() {
        // 获取当前缓存中的所有配置
        Map<String, String> currentConfigs = configCache.getAllConfigs();
        
        // 检查每个绑定的配置项
        for (Map.Entry<String, List<ConfigFieldBinding>> entry : fieldBindings.entrySet()) {
            String configKey = entry.getKey();
            List<ConfigFieldBinding> bindings = entry.getValue();
            
            String newValue = currentConfigs.get(configKey);
            if (newValue != null) {
                // 检查是否需要更新
                for (ConfigFieldBinding binding : bindings) {
                    if (shouldUpdateField(binding, newValue)) {
                        updateFieldValue(binding);
                    }
                }
            }
        }
        
        // 检查方法绑定
        for (Map.Entry<String, List<ConfigMethodBinding>> entry : methodBindings.entrySet()) {
            String configKey = entry.getKey();
            List<ConfigMethodBinding> bindings = entry.getValue();
            
            String newValue = currentConfigs.get(configKey);
            if (newValue != null) {
                for (ConfigMethodBinding binding : bindings) {
                    if (shouldUpdateMethod(binding, newValue)) {
                        updateMethodValue(binding);
                    }
                }
            }
        }
    }
    
    /**
     * 检查字段是否需要更新
     */
    private boolean shouldUpdateField(ConfigFieldBinding binding, String newValue) {
        try {
            Object currentValue = binding.field.get(binding.target);
            return !Objects.equals(String.valueOf(currentValue), newValue);
        } catch (Exception e) {
            logger.error("检查字段值失败", e);
            return false;
        }
    }
    
    /**
     * 检查方法是否需要更新
     */
    private boolean shouldUpdateMethod(ConfigMethodBinding binding, String newValue) {
        // 这里可以实现更复杂的逻辑来判断是否需要调用方法
        return true;
    }
    
    /**
     * 更新字段值
     */
    private void updateFieldValue(ConfigFieldBinding binding) {
        try {
            String configValue = configCache.get(binding.configKey);
            if (configValue != null) {
                Object convertedValue = convertValue(configValue, binding.field.getType());
                binding.field.set(binding.target, convertedValue);
                
                logger.debug("热更新字段: {}.{} = {}", 
                    binding.target.getClass().getSimpleName(), 
                    binding.field.getName(), 
                    configValue);
            }
        } catch (Exception e) {
            logger.error("更新字段值失败: {}.{}", 
                binding.target.getClass().getSimpleName(), 
                binding.field.getName(), e);
        }
    }
    
    /**
     * 更新方法值
     */
    private void updateMethodValue(ConfigMethodBinding binding) {
        try {
            String configValue = configCache.get(binding.configKey);
            if (configValue != null) {
                // 根据方法参数类型转换值
                Class<?>[] paramTypes = binding.method.getParameterTypes();
                Object[] args = new Object[paramTypes.length];
                
                for (int i = 0; i < paramTypes.length; i++) {
                    args[i] = convertValue(configValue, paramTypes[i]);
                }
                
                binding.method.invoke(binding.target, args);
                
                logger.debug("热更新方法: {}.{}()", 
                    binding.target.getClass().getSimpleName(), 
                    binding.method.getName());
            }
        } catch (Exception e) {
            logger.error("更新方法值失败: {}.{}()", 
                binding.target.getClass().getSimpleName(), 
                binding.method.getName(), e);
        }
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
     * 获取字段（包括父类字段）
     */
    private Field getField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                return getField(superClass, fieldName);
            }
            return null;
        }
    }
    
    /**
     * 停止热更新管理器
     */
    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        logger.info("配置热更新管理器已停止");
    }
    
    /**
     * 配置字段绑定
     */
    private static class ConfigFieldBinding {
        final Object target;
        final Field field;
        final String configKey;
        
        ConfigFieldBinding(Object target, Field field, String configKey) {
            this.target = target;
            this.field = field;
            this.configKey = configKey;
        }
    }
    
    /**
     * 配置方法绑定
     */
    private static class ConfigMethodBinding {
        final Object target;
        final java.lang.reflect.Method method;
        final String configKey;
        
        ConfigMethodBinding(Object target, java.lang.reflect.Method method, String configKey) {
            this.target = target;
            this.method = method;
            this.configKey = configKey;
        }
    }
}
