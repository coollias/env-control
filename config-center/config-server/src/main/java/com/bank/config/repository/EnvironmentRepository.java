package com.bank.config.repository;

import com.bank.config.entity.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 环境Repository
 * 
 * @author bank
 */
@Repository
public interface EnvironmentRepository extends JpaRepository<Environment, Long> {

    /**
     * 根据环境编码查找环境
     */
    Optional<Environment> findByEnvCode(String envCode);

    /**
     * 根据环境编码和状态查找环境
     */
    Optional<Environment> findByEnvCodeAndStatus(String envCode, Integer status);

    /**
     * 根据状态查找环境列表，按排序字段排序
     */
    List<Environment> findByStatusOrderBySortOrderAsc(Integer status);

    /**
     * 根据状态分页查找环境列表，按排序字段排序
     */
    Page<Environment> findByStatusOrderBySortOrderAsc(Integer status, Pageable pageable);

    /**
     * 检查环境编码是否存在
     */
    boolean existsByEnvCode(String envCode);

    /**
     * 根据状态统计环境数量
     */
    long countByStatus(Integer status);
} 