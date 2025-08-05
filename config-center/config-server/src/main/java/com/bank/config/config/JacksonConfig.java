package com.bank.config.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Jackson配置类
 * 
 * @author bank
 */
@Configuration
public class JacksonConfig {

    /**
     * 配置ObjectMapper，添加PageImpl的序列化支持
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 注册JSR310模块（Java 8日期时间支持）
        mapper.registerModule(new JSR310Module());
        
        // 添加PageImpl的Mixin
        mapper.addMixIn(PageImpl.class, PageImplMixin.class);
        
        return mapper;
    }
    
    /**
     * PageImpl的Mixin类
     */
    abstract static class PageImplMixin {
        @JsonCreator
        public PageImplMixin(
                @JsonProperty("content") List<?> content,
                @JsonProperty("pageable") Pageable pageable,
                @JsonProperty("total") long total) {
        }
    }
} 