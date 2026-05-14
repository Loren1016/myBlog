package com.myblog.service;

import com.myblog.entity.Project;
import com.myblog.exception.ResourceNotFoundException;
import com.myblog.repository.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    public Project findBySlug(String slug) {
        return projectRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "slug", slug));
    }

    public Project create(Project project) {
        project.setId(null);
        project.setCreatedAt(null);
        project.setUpdatedAt(null);
        return projectRepository.save(project);
    }

    public Project update(String id, Project updated) {
        Project existing = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        existing.setName(updated.getName());
        existing.setSlug(updated.getSlug());
        existing.setTagline(updated.getTagline());
        existing.setDescription(updated.getDescription());
        existing.setCoverImage(updated.getCoverImage());
        existing.setScreenshots(updated.getScreenshots());
        existing.setTechStack(updated.getTechStack());
        existing.setDemoUrl(updated.getDemoUrl());
        existing.setSourceUrl(updated.getSourceUrl());
        existing.setPriority(updated.getPriority());
        existing.setRole(updated.getRole());
        existing.setCompletedAt(updated.getCompletedAt());
        return projectRepository.save(existing);
    }

    public Project findById(String id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
    }

    public Page<Project> findAllPaginated(int page, int size) {
        int p = Math.max(page, 0);
        int s = Math.min(Math.max(size, 1), 100);
        return projectRepository.findAll(
                PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "priority")
                        .and(Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    public void delete(String id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        projectRepository.delete(project);
    }
}
