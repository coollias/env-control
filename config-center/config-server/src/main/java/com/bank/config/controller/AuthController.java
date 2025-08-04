package com.bank.config.controller;

import com.bank.config.common.ApiResponse;
import com.bank.config.dto.AuthResponse;
import com.bank.config.dto.LoginRequest;
import com.bank.config.dto.RegisterRequest;
import com.bank.config.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 认证控制器
 * 
 * @author bank
 */
@RestController
@RequestMapping("/api/auth")

public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = userService.register(request);
            return ResponseEntity.ok(ApiResponse.success("注册成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = userService.login(request);
            return ResponseEntity.ok(ApiResponse.success("登录成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 检查用户名是否可用
     */
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Boolean>> checkUsername(@RequestParam String username) {
        boolean available = !userService.existsByUsername(username);
        return ResponseEntity.ok(ApiResponse.success("用户名检查完成", available));
    }

    /**
     * 检查邮箱是否可用
     */
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@RequestParam String email) {
        boolean available = !userService.existsByEmail(email);
        return ResponseEntity.ok(ApiResponse.success("邮箱检查完成", available));
    }
} 