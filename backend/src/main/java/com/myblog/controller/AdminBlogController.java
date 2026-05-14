package com.myblog.controller;

import com.myblog.dto.ApiResponse;
import com.myblog.entity.Blog;
import com.myblog.service.BlogService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/posts")
public class AdminBlogController {

    private final BlogService blogService;

    public AdminBlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Blog> result = blogService.findAllPaginated(page, size);
        return ApiResponse.success(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "page", result.getNumber()
        ));
    }

    @GetMapping("/{id}")
    public ApiResponse<Blog> get(@PathVariable String id) {
        return ApiResponse.success(blogService.findById(id));
    }

    @PostMapping
    public ApiResponse<Blog> create(@Valid @RequestBody Blog blog) {
        return ApiResponse.success(blogService.create(blog));
    }

    @PutMapping("/{id}")
    public ApiResponse<Blog> update(@PathVariable String id, @Valid @RequestBody Blog blog) {
        return ApiResponse.success(blogService.update(id, blog));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        blogService.delete(id);
        return ApiResponse.success(null);
    }
}
