package com.myblog.dto;

public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private AdminUserDto user;

    public LoginResponse() {}

    public LoginResponse(String accessToken, String refreshToken, AdminUserDto user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public AdminUserDto getUser() { return user; }
    public void setUser(AdminUserDto user) { this.user = user; }
}
