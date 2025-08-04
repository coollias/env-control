package com.bank.config.dto;

/**
 * 认证响应DTO
 * 
 * @author bank
 */
public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserInfo userInfo;

    public AuthResponse() {
    }

    public AuthResponse(String token, Long expiresIn, UserInfo userInfo) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.userInfo = userInfo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    /**
     * 用户信息内部类
     */
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String realName;
        private String phone;

        public UserInfo() {
        }

        public UserInfo(Long id, String username, String email, String realName, String phone) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.realName = realName;
            this.phone = phone;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getRealName() {
            return realName;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
} 