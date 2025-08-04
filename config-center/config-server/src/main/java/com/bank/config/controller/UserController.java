package com.bank.config.controller;

import com.bank.config.common.ApiResponse;
import com.bank.config.entity.User;
import com.bank.config.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 * 
 * @author bank
 */
@RestController
@RequestMapping("/api/users")

public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取所有用户
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        return ResponseEntity.ok(ApiResponse.success("获取用户列表成功", users));
    }

    /**
     * 获取所有用户（用于权限管理）
     */
    @GetMapping("/for-permissions")
    public ResponseEntity<ApiResponse<List<User>>> getUsersForPermissions() {
        List<User> users = userService.findAllUsers();
        return ResponseEntity.ok(ApiResponse.success("获取用户列表成功", users));
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> ResponseEntity.ok(ApiResponse.success("获取用户成功", user)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 创建用户
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.ok(ApiResponse.success("创建用户成功", createdUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(ApiResponse.success("更新用户成功", updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.success("删除用户成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Boolean>> checkUsername(@RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(ApiResponse.success("用户名检查完成", exists));
    }

    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(ApiResponse.success("邮箱检查完成", exists));
    }
} 