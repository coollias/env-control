package com.bank.config.service.impl;

import com.bank.config.common.JwtUtils;
import com.bank.config.dto.AuthResponse;
import com.bank.config.dto.LoginRequest;
import com.bank.config.dto.RegisterRequest;
import com.bank.config.entity.Role;
import com.bank.config.entity.User;
import com.bank.config.repository.UserRepository;
import com.bank.config.service.RoleService;
import com.bank.config.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户服务实现类
 * 
 * @author bank
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;

    @Autowired
    @Lazy
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    private JwtUtils jwtUtils;

    @Autowired
    @Lazy
    public void setJwtUtils(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Autowired
    private RoleService roleService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        // 验证密码确认
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("密码和确认密码不匹配");
        }

        // 检查用户名是否已存在
        if (existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (request.getEmail() != null && existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setStatus(1);

        User savedUser = userRepository.save(user);

        // 为新用户分配默认角色（VIEWER）
        try {
            Optional<Role> defaultRole = roleService.findByRoleCode("VIEWER");
            if (defaultRole.isPresent()) {
                roleService.assignRoleToUser(savedUser.getId(), defaultRole.get().getId());
            }
        } catch (Exception e) {
            // 如果分配角色失败，记录日志但不影响注册流程
            System.err.println("为新用户分配默认角色失败: " + e.getMessage());
        }

        // 生成JWT令牌
        String token = jwtUtils.generateToken(savedUser.getUsername(), savedUser.getId());

        // 构建响应
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRealName(),
                savedUser.getPhone()
        );

        return new AuthResponse(token, 86400L, userInfo);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // 查找用户
        Optional<User> userOpt = userRepository.findByUsernameAndStatus(request.getUsername(), 1);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户名或密码错误");
        }

        User user = userOpt.get();

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 更新最后登录时间
        updateLastLoginTime(user.getId());

        // 生成JWT令牌
        String token = jwtUtils.generateToken(user.getUsername(), user.getId());

        // 构建响应
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRealName(),
                user.getPhone()
        );

        return new AuthResponse(token, 86400L, userInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User createUser(User user) {
        // 检查用户名是否已存在
        if (existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (user.getEmail() != null && existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1);

        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long id, User user) {
        Optional<User> existingUser = userRepository.findById(id);
        if (!existingUser.isPresent()) {
            throw new RuntimeException("用户不存在");
        }

        User existing = existingUser.get();

        // 检查用户名是否与其他用户冲突
        if (!existing.getUsername().equals(user.getUsername()) && 
            existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否与其他用户冲突
        if (user.getEmail() != null && !user.getEmail().equals(existing.getEmail()) && 
            existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }

        // 更新字段
        existing.setUsername(user.getUsername());
        existing.setEmail(user.getEmail());
        existing.setRealName(user.getRealName());
        existing.setPhone(user.getPhone());

        // 如果提供了新密码，则更新密码
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(existing);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("用户不存在");
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findByStatusOrderByCreatedAtDesc(1);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void updateLastLoginTime(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }
} 