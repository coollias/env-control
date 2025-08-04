package com.bank.config.service;

import com.bank.config.entity.ConfigItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 文件解析服务
 * 支持解析XML、YAML、Properties、JSON等格式的配置文件
 * 
 * @author bank
 */
@Service
public class FileParseService {

    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final XmlMapper xmlMapper = new XmlMapper();

    /**
     * 解析配置文件
     * 
     * @param file 上传的文件
     * @param appId 应用ID
     * @param envId 环境ID
     * @return 解析后的配置项列表
     */
    public List<ConfigItem> parseConfigFile(MultipartFile file, Long appId, Long envId) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String fileExtension = getFileExtension(fileName);
        String content = new String(file.getBytes(), "UTF-8");

        switch (fileExtension.toLowerCase()) {
            case "json":
                return parseJsonFile(content, appId, envId);
            case "yaml":
            case "yml":
                return parseYamlFile(content, appId, envId);
            case "xml":
                return parseXmlFile(content, appId, envId);
            case "properties":
                return parsePropertiesFile(content, appId, envId);
            default:
                throw new IllegalArgumentException("不支持的文件格式: " + fileExtension);
        }
    }

    /**
     * 解析JSON文件
     */
    private List<ConfigItem> parseJsonFile(String content, Long appId, Long envId) throws IOException {
        List<ConfigItem> configItems = new ArrayList<>();
        
        try {
            Map<String, Object> jsonMap = jsonMapper.readValue(content, Map.class);
            parseMapToConfigItems(jsonMap, "", configItems, appId, envId);
        } catch (Exception e) {
            throw new IOException("JSON文件解析失败: " + e.getMessage());
        }
        
        return configItems;
    }

    /**
     * 解析YAML文件
     */
    private List<ConfigItem> parseYamlFile(String content, Long appId, Long envId) throws IOException {
        List<ConfigItem> configItems = new ArrayList<>();
        
        try {
            Map<String, Object> yamlMap = yamlMapper.readValue(content, Map.class);
            parseMapToConfigItems(yamlMap, "", configItems, appId, envId);
        } catch (Exception e) {
            throw new IOException("YAML文件解析失败: " + e.getMessage());
        }
        
        return configItems;
    }

    /**
     * 解析XML文件
     */
    private List<ConfigItem> parseXmlFile(String content, Long appId, Long envId) throws IOException {
        List<ConfigItem> configItems = new ArrayList<>();
        
        try {
            Map<String, Object> xmlMap = xmlMapper.readValue(content, Map.class);
            parseMapToConfigItems(xmlMap, "", configItems, appId, envId);
        } catch (Exception e) {
            throw new IOException("XML文件解析失败: " + e.getMessage());
        }
        
        return configItems;
    }

    /**
     * 解析Properties文件
     */
    private List<ConfigItem> parsePropertiesFile(String content, Long appId, Long envId) throws IOException {
        List<ConfigItem> configItems = new ArrayList<>();
        
        try {
            Properties properties = new Properties();
            properties.load(new java.io.ByteArrayInputStream(content.getBytes("UTF-8")));
            
            for (String key : properties.stringPropertyNames()) {
                ConfigItem configItem = new ConfigItem();
                configItem.setAppId(appId);
                configItem.setEnvId(envId);
                configItem.setConfigKey(key);
                configItem.setConfigValue(properties.getProperty(key));
                configItem.setConfigType(1); // 字符串类型
                configItem.setIsRequired(0);
                configItem.setIsEncrypted(0);
                configItem.setStatus(1);
                configItem.setDescription("从Properties文件导入");
                
                configItems.add(configItem);
            }
        } catch (Exception e) {
            throw new IOException("Properties文件解析失败: " + e.getMessage());
        }
        
        return configItems;
    }

    /**
     * 递归解析Map结构，将嵌套的配置转换为扁平化的配置项
     */
    private void parseMapToConfigItems(Map<String, Object> map, String prefix, List<ConfigItem> configItems, Long appId, Long envId) throws JsonProcessingException {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
            
            if (value instanceof Map) {
                // 递归处理嵌套的Map
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                parseMapToConfigItems(nestedMap, fullKey, configItems, appId, envId);
            } else if (value instanceof List) {
                // 处理List类型，转换为JSON字符串
                ConfigItem configItem = new ConfigItem();
                configItem.setAppId(appId);
                configItem.setEnvId(envId);
                configItem.setConfigKey(fullKey);
                configItem.setConfigValue(jsonMapper.writeValueAsString(value));
                configItem.setConfigType(4); // JSON类型
                configItem.setIsRequired(0);
                configItem.setIsEncrypted(0);
                configItem.setStatus(1);
                configItem.setDescription("从配置文件导入");
                
                configItems.add(configItem);
            } else {
                // 处理基本类型
                ConfigItem configItem = new ConfigItem();
                configItem.setAppId(appId);
                configItem.setEnvId(envId);
                configItem.setConfigKey(fullKey);
                configItem.setConfigValue(value != null ? value.toString() : "");
                configItem.setConfigType(1); // 字符串类型
                configItem.setIsRequired(0);
                configItem.setIsEncrypted(0);
                configItem.setStatus(1);
                configItem.setDescription("从配置文件导入");
                
                configItems.add(configItem);
            }
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }

    /**
     * 验证文件格式是否支持
     */
    public boolean isSupportedFileFormat(String fileName) {
        if (fileName == null) {
            return false;
        }
        
        String extension = getFileExtension(fileName).toLowerCase();
        return Arrays.asList("json", "yaml", "yml", "xml", "properties").contains(extension);
    }

    /**
     * 获取支持的文件格式列表
     */
    public List<String> getSupportedFormats() {
        return Arrays.asList("JSON (.json)", "YAML (.yaml/.yml)", "XML (.xml)", "Properties (.properties)");
    }
} 