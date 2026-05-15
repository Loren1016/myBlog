package com.myblog.controller;

import com.myblog.dto.AboutProfileDto;
import com.myblog.dto.ApiResponse;
import com.myblog.service.AboutProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/about")
public class AboutController {

    private final AboutProfileService aboutProfileService;

    public AboutController(AboutProfileService aboutProfileService) {
        this.aboutProfileService = aboutProfileService;
    }

    @GetMapping("/profile")
    public ApiResponse<AboutProfileDto> getProfile() {
        return ApiResponse.success(aboutProfileService.getPublicProfile());
    }
}
