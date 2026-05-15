package com.myblog.controller;

import com.myblog.dto.AboutProfileDto;
import com.myblog.dto.ApiResponse;
import com.myblog.service.AboutProfileService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/about")
public class AdminAboutController {

    private final AboutProfileService aboutProfileService;

    public AdminAboutController(AboutProfileService aboutProfileService) {
        this.aboutProfileService = aboutProfileService;
    }

    @GetMapping("/profile")
    public ApiResponse<AboutProfileDto> getProfile() {
        return ApiResponse.success(aboutProfileService.getAdminProfile());
    }

    @PutMapping("/profile")
    public ApiResponse<AboutProfileDto> updateProfile(@RequestBody AboutProfileDto dto) {
        return ApiResponse.success(aboutProfileService.updateProfile(dto));
    }
}
