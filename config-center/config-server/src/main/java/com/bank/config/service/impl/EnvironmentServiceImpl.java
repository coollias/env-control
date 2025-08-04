package com.bank.config.service.impl;

import com.bank.config.entity.Environment;
import com.bank.config.repository.EnvironmentRepository;
import com.bank.config.service.EnvironmentService;
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
        
        return environmentRepository.save(environment);
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
        
        return environmentRepository.save(existing);
    }

    @Override
    public void deleteEnvironment(Long id) {
        if (!environmentRepository.existsById(id)) {
            throw new RuntimeException("环境不存在: " + id);
        }
        environmentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Environment> findById(Long id) {
        return environmentRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Environment> findByEnvCode(String envCode) {
        return environmentRepository.findByEnvCode(envCode);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Environment> findEnvironments(Integer status, Pageable pageable) {
        return environmentRepository.findByStatusOrderBySortOrderAsc(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Environment> findAllEnabled() {
        return environmentRepository.findByStatusOrderBySortOrderAsc(1);
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
} 