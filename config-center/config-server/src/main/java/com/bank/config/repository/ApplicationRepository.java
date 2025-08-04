package com.bank.config.repository;

import com.bank.config.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 应用Repository
 * 
 * @author bank
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    /**
     * 根据应用编码查找应用
     */
    Optional<Application> findByAppCode(String appCode);

    /**
     * 根据应用编码和状态查找应用
     */
    Optional<Application> findByAppCodeAndStatus(String appCode, Integer status);

    /**
     * 根据状态查找应用列表
     */
    List<Application> findByStatusOrderByCreatedAtDesc(Integer status);

    /**
     * 分页查询应用
     */
    Page<Application> findByStatusOrderByCreatedAtDesc(Integer status, Pageable pageable);

    /**
     * 根据应用名称模糊查询
     */
    @Query("SELECT a FROM Application a WHERE a.status = :status AND (a.appName LIKE %:keyword% OR a.appCode LIKE %:keyword%)")
    Page<Application> findByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") Integer status, Pageable pageable);

    /**
     * 检查应用编码是否存在
     */
    boolean existsByAppCode(String appCode);

    /**
     * 根据负责人查找应用
     */
    List<Application> findByOwnerAndStatus(String owner, Integer status);

    /**
     * 根据状态统计应用数量
     */
    long countByStatus(Integer status);

    /**
     * 根据ID列表和状态查找应用
     */
    List<Application> findByIdInAndStatusOrderByCreatedAtDesc(List<Long> ids, Integer status);

    /**
     * 根据创建者和状态查找应用
     */
    List<Application> findByCreatedByAndStatusOrderByCreatedAtDesc(Long createdBy, Integer status);
} 