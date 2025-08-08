package com.bank.config.client;

import com.bank.config.client.cache.ConfigCache;
import com.bank.config.client.fallback.DefaultConfigFallback;
import com.bank.config.client.metrics.ConfigMetrics;
import com.bank.config.client.parser.ConfigConverter;
import com.bank.config.client.parser.ConfigParser;
import com.bank.config.client.retry.ConfigRetry;
import com.bank.config.client.security.ConfigSecurity;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;

/**
 * 配置客户端测试类
 * 
 * @author bank
 */
public class ConfigClientTest {

    @Test
    public void testConfigCache() {
        ConfigCache cache = new ConfigCache("/tmp/test-cache.properties", 300000);
        
        // 测试基本操作
        cache.put("test.key", "test.value");
        assertEquals("test.value", cache.get("test.key"));
        
        // 测试更新配置
        Map<String, String> newConfigs = new HashMap<>();
        newConfigs.put("key1", "value1");
        newConfigs.put("key2", "value2");
        cache.updateConfigs(newConfigs);
        assertEquals("value1", cache.get("key1"));
        assertEquals("value2", cache.get("key2"));
        
        // 测试缓存大小
        assertEquals(2, cache.size());
        
        // 测试缓存是否为空
        assertFalse(cache.isEmpty());
        
        // 测试清空缓存
        cache.clear();
        assertTrue(cache.isEmpty());
    }

    @Test
    public void testConfigParser() {
        ConfigParser parser = new ConfigParser();
        
        // 测试Properties格式解析
        String propertiesContent = "key1=value1\nkey2=value2";
        Map<String, String> propsResult = parser.parseProperties(propertiesContent);
        assertEquals("value1", propsResult.get("key1"));
        assertEquals("value2", propsResult.get("key2"));
        
        // 测试JSON格式解析
        String jsonContent = "{\"key1\":\"value1\",\"key2\":\"value2\"}";
        Map<String, String> jsonResult = parser.parseJson(jsonContent);
        assertEquals("value1", jsonResult.get("key1"));
        assertEquals("value2", jsonResult.get("key2"));
        
        // 测试YAML格式解析
        String yamlContent = "key1: value1\nkey2: value2";
        Map<String, String> yamlResult = parser.parseYaml(yamlContent);
        assertEquals("value1", yamlResult.get("key1"));
        assertEquals("value2", yamlResult.get("key2"));
        
        // 测试格式检测
        assertEquals("properties", parser.detectFormat(propertiesContent));
        assertEquals("json", parser.detectFormat(jsonContent));
        assertEquals("yaml", parser.detectFormat(yamlContent));
    }

    @Test
    public void testConfigConverter() {
        ConfigConverter converter = new ConfigConverter();
        
        // 测试基本类型转换
        assertEquals("test", converter.convert("test", String.class));
        assertEquals(Integer.valueOf(123), converter.convert("123", Integer.class));
        assertEquals(Long.valueOf(456), converter.convert("456", Long.class));
        assertEquals(Double.valueOf(7.89), converter.convert("7.89", Double.class));
        assertEquals(Boolean.TRUE, converter.convert("true", Boolean.class));
        
        // 测试列表转换
        assertEquals(3, converter.convertToList("a,b,c").size());
        
        // 测试Map转换
        Map<String, String> map = converter.convertToMap("key1=value1,key2=value2");
        assertEquals("value1", map.get("key1"));
        assertEquals("value2", map.get("key2"));
        
        // 测试安全转换
        assertEquals("invalid", converter.convertSafely("invalid", String.class, "default")); // 字符串转换总是成功的
        assertEquals(Integer.valueOf(0), converter.convertSafely("invalid", Integer.class, 0)); // 数字转换失败时返回默认值
        
        // 测试转换检查
        assertTrue(converter.canConvert("123", Integer.class));
        assertFalse(converter.canConvert("invalid", Integer.class));
    }

    @Test
    public void testConfigSecurity() {
        ConfigSecurity security = new ConfigSecurity("test-token", "test-app", "test-env");
        
        // 测试加密解密
        String originalValue = "sensitive-data";
        String encrypted = security.encrypt(originalValue);
        String decrypted = security.decrypt(encrypted);
        assertEquals(originalValue, decrypted);
        
        // 测试加密检测
        assertTrue(security.isEncrypted(encrypted));
        assertFalse(security.isEncrypted(originalValue));
        
        // 测试签名生成
        String signature = security.generateSignature("1234567890", "nonce");
        assertNotNull(signature);
        assertFalse(signature.isEmpty());
    }

    @Test
    public void testConfigRetry() {
        ConfigRetry retry = new ConfigRetry(3, 100, 2.0, 1000);
        
        // 测试成功执行
        String result = retry.executeWithRetry((Callable<String>) () -> "success");
        assertEquals("success", result);
        
        // 测试重试机制（模拟失败后成功）
        final int[] attempts = {0};
        String retryResult = retry.executeWithRetry((Callable<String>) () -> {
            attempts[0]++;
            if (attempts[0] < 3) {
                throw new RuntimeException("模拟失败");
            }
            return "retry-success";
        });
        assertEquals("retry-success", retryResult);
        assertEquals(3, attempts[0]);
    }

    @Test
    public void testConfigMetrics() {
        ConfigMetrics metrics = new ConfigMetrics();
        
        // 测试拉取指标
        metrics.recordPull();
        metrics.recordPullSuccess();
        metrics.recordPullLatency(100);
        
        Map<String, Object> metricsData = metrics.getMetrics();
        assertEquals(1L, metricsData.get("pull.total"));
        assertEquals(1L, metricsData.get("pull.success"));
        assertEquals(100.0, metricsData.get("pull.averageLatency"));
        
        // 测试缓存指标
        metrics.recordCacheHit();
        metrics.recordCacheMiss();
        metrics.updateCacheSize(10);
        
        metricsData = metrics.getMetrics();
        assertEquals(1L, metricsData.get("cache.hits"));
        assertEquals(1L, metricsData.get("cache.misses"));
        assertEquals(10L, metricsData.get("cache.size"));
        
        // 测试健康状态
        Map<String, Object> health = metrics.getHealthStatus();
        assertNotNull(health.get("status"));
    }

    @Test
    public void testConfigFallback() {
        DefaultConfigFallback fallback = new DefaultConfigFallback();
        
        // 测试默认值
        assertEquals("jdbc:mysql://localhost:3306/test", fallback.getDefaultValue("database.url"));
        assertEquals("localhost", fallback.getDefaultValue("redis.host"));
        assertEquals("INFO", fallback.getDefaultValue("logging.level"));
        
        // 测试默认值检查
        assertTrue(fallback.hasDefaultValue("database.url"));
        assertFalse(fallback.hasDefaultValue("nonexistent.key"));
        
        // 测试所有默认值
        Map<String, String> allDefaults = fallback.getAllDefaultValues();
        assertFalse(allDefaults.isEmpty());
        assertTrue(allDefaults.containsKey("database.url"));
        
        // 测试添加自定义默认值
        fallback.addDefaultValue("custom.key", "custom.value");
        assertEquals("custom.value", fallback.getDefaultValue("custom.key"));
    }

    @Test
    public void testConfigClientBuilder() {
        // 测试构建器
        ConfigClient client = new ConfigClient.ConfigClientBuilder()
            .serverUrl("http://localhost:8080")
            .appCode("test-app")
            .envCode("test-env")
            .token("test-token")
            .pollInterval(30000)
            .cacheFile("/tmp/test-cache.properties")
            .enablePolling(true)
            .enableCache(true)
            .build();
        
        // 验证基本属性
        assertEquals("http://localhost:8080", client.getServerUrl());
        assertEquals("test-app", client.getAppCode());
        assertEquals("test-env", client.getEnvCode());
        
        // 验证组件初始化
        assertNotNull(client.getCache());
        assertNotNull(client.getPoller());
        assertNotNull(client.getMetrics());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConfigClientBuilderValidation() {
        // 测试缺少必需参数时抛出异常
        new ConfigClient.ConfigClientBuilder()
            .serverUrl("http://localhost:8080")
            // 缺少appCode和envCode
            .build();
    }
} 