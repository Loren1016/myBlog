package com.myblog.controller;

import com.myblog.dto.ApiResponse;
import com.myblog.entity.Media;
import com.myblog.service.MediaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/media")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping("/upload")
    public ApiResponse<Media> upload() {
        // TODO: Implement file upload
        return ApiResponse.success(null);
    }
}
