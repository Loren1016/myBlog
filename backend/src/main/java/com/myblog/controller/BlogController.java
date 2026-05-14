package com.myblog.controller;

import com.myblog.dto.ApiResponse;
import com.myblog.dto.BlogDto;
import com.myblog.entity.Blog;
import com.myblog.service.BlogService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class BlogController {

    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping({"/api/blogs", "/api/posts"})
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Blog> result = blogService.findPublished(page, size);
        return ApiResponse.success(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "page", result.getNumber()
        ));
    }

    @GetMapping({"/api/blogs/{slug}", "/api/posts/{slug}"})
    public ApiResponse<Blog> getBySlug(@PathVariable String slug) {
        return ApiResponse.success(blogService.findBySlugPublished(slug));
    }
}
