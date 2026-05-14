package com.myblog.controller;

import com.myblog.dto.ApiResponse;
import com.myblog.entity.Project;
import com.myblog.service.ProjectService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ApiResponse<List<Project>> list() {
        return ApiResponse.success(projectService.findAll());
    }

    @GetMapping("/{slug}")
    public ApiResponse<Project> getBySlug(@PathVariable String slug) {
        return ApiResponse.success(projectService.findBySlug(slug));
    }
}
