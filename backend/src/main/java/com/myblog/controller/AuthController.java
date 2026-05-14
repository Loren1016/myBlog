package com.myblog.controller;

import com.myblog.dto.*;
import com.myblog.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/admin/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse result = authService.login(request);
        return ApiResponse.success(result);
    }

    @PostMapping("/admin/refresh")
    public ApiResponse<LoginResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        LoginResponse result = authService.refresh(request.getRefreshToken());
        return ApiResponse.success(result);
    }

    @PostMapping("/admin/logout")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.success(null);
    }

    @GetMapping("/admin/me")
    public ApiResponse<AdminUserDto> me() {
        AdminUserDto user = authService.getMe();
        return ApiResponse.success(user);
    }
}
