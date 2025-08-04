package com.bank.config.service;

import com.bank.config.dto.AuthResponse;
import com.bank.config.dto.LoginRequest;
import com.bank.config.dto.RegisterRequest;
import com.bank.config.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务接口
 * 
 * @author bank
 */
public interface UserService {

    /**
     * 用户注册
     */
    AuthResponse register(RegisterRequest request);

    /**
     * 用户登录
     */
    AuthResponse login(LoginRequest request);

    /**
     * 根据ID查找用户
     */
    Optional<User> findById(Long id);

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);

    /**
     * 创建用户
     */
    User createUser(User user);

    /**
     * 更新用户
     */
    User updateUser(Long id, User user);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 获取所有用户
     */
    List<User> findAllUsers();

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 更新用户最后登录时间
     */
    void updateLastLoginTime(Long userId);
} 