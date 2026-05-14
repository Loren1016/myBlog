package com.myblog.controller;

import com.myblog.dto.ApiResponse;
import com.myblog.dto.MediaUploadResponse;
import com.myblog.service.MediaService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/media")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping("/upload")
    public ApiResponse<MediaUploadResponse> upload(@RequestParam("file") MultipartFile file) {
        MediaUploadResponse result = mediaService.upload(file);
        return ApiResponse.success(result);
    }
}
