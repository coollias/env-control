package com.bank.config.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA配置类
 * 
 * @author bank
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.bank.config.repository")
public class JpaConfig {
} 