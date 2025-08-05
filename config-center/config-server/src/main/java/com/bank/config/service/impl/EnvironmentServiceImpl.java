package com.bank.config.service.impl;

import com.bank.config.entity.Environment;
import com.bank.config.repository.EnvironmentRepository;
import com.bank.config.service.EnvironmentService;
import com.bank.config.service.RedisCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 环境Service实现类
 * 
 * @author bank
 */
@Service
@Transactional
public class EnvironmentServiceImpl implements EnvironmentService {

    @Autowired
    private EnvironmentRepository environmentRepository;

    @Autowired
    private RedisCacheService redisCacheService;

    @Override
    public Environment createEnvironment(Environment environment) {
        // 检查环境编码是否已存在
        if (existsByEnvCode(environment.getEnvCode())) {
            throw new RuntimeException("环境编码已存在: " + environment.getEnvCode());
        }
        
        // 设置默认状态
        if (environment.getStatus() == null) {
            environment.setStatus(1);
        }
        
        Environment saved = environmentRepository.save(environment);
        
        // 清理相关缓存
        redisCacheService.deleteByPattern("env*");
        
        return saved;
    }

    @Override
    public Environment updateEnvironment(Long id, Environment environment) {
        Optional<Environment> existingEnv = environmentRepository.findById(id);
        if (!existingEnv.isPresent()) {
            throw new RuntimeException("环境不存在: " + id);
        }
        
        Environment existing = existingEnv.get();
        
        // 检查环境编码是否与其他环境冲突
        if (!existing.getEnvCode().equals(environment.getEnvCode()) && 
            existsByEnvCode(environment.getEnvCode())) {
            throw new RuntimeException("环境编码已存在: " + environment.getEnvCode());
        }
        
        // 更新字段
        existing.setEnvCode(environment.getEnvCode());
        existing.setEnvName(environment.getEnvName());
        existing.setEnvDesc(environment.getEnvDesc());
        existing.setSortOrder(environment.getSortOrder());
        
        Environment saved = environmentRepository.save(existing);
        
        // 清理相关缓存
        redisCacheService.deleteByPattern("env*");
        
        return saved;
    }

    @Override
    public void deleteEnvironment(Long id) {
        if (!environmentRepository.existsById(id)) {
            throw new RuntimeException("环境不存在: " + id);
        }
        environmentRepository.deleteById(id);
        
        // 清理相关缓存
        redisCacheService.deleteByPattern("env*");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Environment> findById(Long id) {
        // 尝试从缓存获取
        String cacheKey = "env:" + id;
        Optional<Environment> cached = redisCacheService.get(cacheKey, Environment.class);
        if (cached.isPresent()) {
            System.out.println("=== 环境缓存命中: " + cacheKey + " ===");
            return cached;
        }
        
        System.out.println("=== 环境缓存未命中: " + cacheKey + " ===");
        
        // 从数据库获取
        Optional<Environment> env = environmentRepository.findById(id);
        if (env.isPresent()) {
            // 存入缓存
            redisCacheService.set(cacheKey, env.get());
            System.out.println("=== 环境已缓存: " + cacheKey + " ===");
        }
        
        return env;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Environment> findByEnvCode(String envCode) {
        // 尝试从缓存获取
        String cacheKey = "env:code:" + envCode;
        Optional<Environment> cached = redisCacheService.get(cacheKey, Environment.class);
        if (cached.isPresent()) {
            return cached;
        }
        
        // 从数据库获取
        Optional<Environment> env = environmentRepository.findByEnvCode(envCode);
        if (env.isPresent()) {
            // 存入缓存
            redisCacheService.set(cacheKey, env.get());
        }
        
        return env;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Environment> findEnvironments(Integer status, Pageable pageable) {
        // 生成简单的缓存键
        String cacheKey = "env:list:" + status + ":" + pageable.getPageNumber() + ":" + pageable.getPageSize();
        
        // 尝试从缓存获取
        Optional<Page<Environment>> cached = redisCacheService.get(cacheKey, new com.fasterxml.jackson.core.type.TypeReference<Page<Environment>>() {});
        if (cached.isPresent()) {
            System.out.println("=== 环境列表缓存命中: " + cacheKey + " ===");
            return cached.get();
        }
        
        System.out.println("=== 环境列表缓存未命中: " + cacheKey + " ===");
        
        // 从数据库获取
        Page<Environment> result = environmentRepository.findByStatusOrderBySortOrderAsc(status, pageable);
        
        // 存入缓存
        redisCacheService.set(cacheKey, result);
        System.out.println("=== 环境列表已缓存: " + cacheKey + " ===");
        
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Environment> findAllEnabled() {
        // 尝试从缓存获取
        String cacheKey = "env:enabled";
        System.out.println("=== 尝试获取环境列表缓存: " + cacheKey + " ===");
        
        Optional<List<Environment>> cached = redisCacheService.get(cacheKey, new com.fasterxml.jackson.core.type.TypeReference<List<Environment>>() {});
        if (cached.isPresent()) {
            System.out.println("=== 环境列表缓存命中: " + cacheKey + " ===");
            return cached.get();
        }
        
        System.out.println("=== 环境列表缓存未命中: " + cacheKey + " ===");
        
        // 从数据库获取
        List<Environment> envs = environmentRepository.findByStatusOrderBySortOrderAsc(1);
        System.out.println("=== 从数据库获取到 " + envs.size() + " 个环境 ===");
        
        // 存入缓存
        redisCacheService.set(cacheKey, envs);
        System.out.println("=== 环境列表已缓存: " + cacheKey + " ===");
        
        return envs;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEnvCode(String envCode) {
        return environmentRepository.existsByEnvCode(envCode);
    }

    @Override
    public Environment updateStatus(Long id, Integer status) {
        Optional<Environment> optional = environmentRepository.findById(id);
        if (!optional.isPresent()) {
            throw new RuntimeException("环境不存在: " + id);
        }
        
        Environment environment = optional.get();
        environment.setStatus(status);
        return environmentRepository.save(environment);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAllEnabled() {
        return environmentRepository.countByStatus(1);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Environment> findAllEnvironments() {
        // 尝试从缓存获取
        String cacheKey = "env:all";
        Optional<List<Environment>> cached = redisCacheService.get(cacheKey, new com.fasterxml.jackson.core.type.TypeReference<List<Environment>>() {});
        if (cached.isPresent()) {
            System.out.println("=== 从缓存获取所有环境列表 ===");
            return cached.get();
        }
        
        System.out.println("=== 缓存未命中所有环境列表 ===");
        
        // 从数据库获取
        List<Environment> envs = environmentRepository.findByStatusOrderBySortOrderAsc(1);
        
        // 存入缓存
        redisCacheService.set(cacheKey, envs);
        System.out.println("=== 所有环境列表已缓存 ===");
        
        return envs;
    }
} 