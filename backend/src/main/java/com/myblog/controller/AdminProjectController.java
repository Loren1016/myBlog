package com.myblog.controller;

import com.myblog.dto.ApiResponse;
import com.myblog.entity.Project;
import com.myblog.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/projects")
public class AdminProjectController {

    private final ProjectService projectService;

    public AdminProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Project> result = projectService.findAllPaginated(page, size);
        return ApiResponse.success(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "page", result.getNumber()
        ));
    }

    @GetMapping("/{id}")
    public ApiResponse<Project> get(@PathVariable String id) {
        return ApiResponse.success(projectService.findById(id));
    }

    @PostMapping
    public ApiResponse<Project> create(@Valid @RequestBody Project project) {
        return ApiResponse.success(projectService.create(project));
    }

    @PutMapping("/{id}")
    public ApiResponse<Project> update(@PathVariable String id, @Valid @RequestBody Project project) {
        return ApiResponse.success(projectService.update(id, project));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        projectService.delete(id);
        return ApiResponse.success(null);
    }
}
