package com.bank.config.client.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 配置解析器
 * 支持多种格式的配置解析
 * 
 * @author bank
 */
public class ConfigParser {
    private static final Logger logger = LoggerFactory.getLogger(ConfigParser.class);

    private final ObjectMapper jsonMapper;
    private final ObjectMapper yamlMapper;

    public ConfigParser() {
        this.jsonMapper = new ObjectMapper();
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
    }

    /**
     * 解析Properties格式配置
     */
    public Map<String, String> parseProperties(String content) {
        try {
            Properties props = new Properties();
            props.load(new java.io.StringReader(content));
            
            Map<String, String> result = new HashMap<>();
            for (String key : props.stringPropertyNames()) {
                result.put(key, props.getProperty(key));
            }
            
            return result;
        } catch (Exception e) {
            logger.error("解析Properties配置失败", e);
            throw new RuntimeException("解析Properties配置失败", e);
        }
    }

    /**
     * 解析YAML格式配置
     */
    public Map<String, String> parseYaml(String content) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> yamlMap = yamlMapper.readValue(content, Map.class);
            return flattenMap(yamlMap, "");
        } catch (Exception e) {
            logger.error("解析YAML配置失败", e);
            throw new RuntimeException("解析YAML配置失败", e);
        }
    }

    /**
     * 解析JSON格式配置
     */
    public Map<String, String> parseJson(String content) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> jsonMap = jsonMapper.readValue(content, Map.class);
            return flattenMap(jsonMap, "");
        } catch (Exception e) {
            logger.error("解析JSON配置失败", e);
            throw new RuntimeException("解析JSON配置失败", e);
        }
    }

    /**
     * 解析XML格式配置
     */
    public Map<String, String> parseXml(String content) {
        try {
            // 简单的XML解析，实际项目中可以使用更强大的XML解析库
            Map<String, String> result = new HashMap<>();
            
            // 这里实现简单的XML解析逻辑
            // 由于XML解析比较复杂，这里只是示例
            // 实际项目中可以使用DOM或SAX解析器
            
            return result;
        } catch (Exception e) {
            logger.error("解析XML配置失败", e);
            throw new RuntimeException("解析XML配置失败", e);
        }
    }

    /**
     * 将嵌套Map扁平化为key-value格式
     */
    private Map<String, String> flattenMap(Map<String, Object> map, String prefix) {
        Map<String, String> result = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                result.putAll(flattenMap(nestedMap, key));
            } else if (value instanceof List) {
                // 处理列表类型
                List<?> list = (List<?>) value;
                for (int i = 0; i < list.size(); i++) {
                    Object item = list.get(i);
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> itemMap = (Map<String, Object>) item;
                        result.putAll(flattenMap(itemMap, key + "[" + i + "]"));
                    } else {
                        result.put(key + "[" + i + "]", String.valueOf(item));
                    }
                }
            } else {
                result.put(key, String.valueOf(value));
            }
        }
        
        return result;
    }

    /**
     * 根据文件扩展名自动选择解析器
     */
    public Map<String, String> parseByExtension(String content, String extension) {
        if (extension == null) {
            extension = "";
        }
        
        switch (extension.toLowerCase()) {
            case "properties":
                return parseProperties(content);
            case "yaml":
            case "yml":
                return parseYaml(content);
            case "json":
                return parseJson(content);
            case "xml":
                return parseXml(content);
            default:
                // 默认按Properties格式解析
                return parseProperties(content);
        }
    }

    /**
     * 检测配置格式
     */
    public String detectFormat(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "properties";
        }
        
        String trimmed = content.trim();
        
        // 检测JSON格式
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return "json";
        }
        
        // 检测YAML格式
        if (trimmed.startsWith("---") || trimmed.contains(":")) {
            return "yaml";
        }
        
        // 检测XML格式
        if (trimmed.startsWith("<") && trimmed.endsWith(">")) {
            return "xml";
        }
        
        // 默认Properties格式
        return "properties";
    }
} 