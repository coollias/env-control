package com.bank.config.security;

import com.bank.config.entity.User;
import com.bank.config.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

/**
 * 自定义UserDetailsService
 * 
 * @author bank
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private UserService userService;

    @Autowired
    @Lazy
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOpt = userService.findByUsername(username);
        
        if (!userOpt.isPresent()) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        User user = userOpt.get();
        
        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }

        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getStatus() == 1
        );
    }
} 