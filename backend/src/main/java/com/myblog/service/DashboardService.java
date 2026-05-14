package com.myblog.service;

import com.myblog.dto.dashboard.*;
import com.myblog.entity.Blog;
import com.myblog.entity.Project;
import com.myblog.repository.BlogRepository;
import com.myblog.repository.MediaRepository;
import com.myblog.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final BlogRepository blogRepository;
    private final ProjectRepository projectRepository;
    private final MediaRepository mediaRepository;

    public DashboardService(BlogRepository blogRepository,
                            ProjectRepository projectRepository,
                            MediaRepository mediaRepository) {
        this.blogRepository = blogRepository;
        this.projectRepository = projectRepository;
        this.mediaRepository = mediaRepository;
    }

    public DashboardStatsResponse getStats() {
        PostStatsDto posts = new PostStatsDto();
        posts.setTotal(blogRepository.count());
        posts.setPublished(blogRepository.countByStatus("published"));
        posts.setDraft(blogRepository.countByStatus("draft"));

        ProjectStatsDto projects = new ProjectStatsDto();
        projects.setTotal(projectRepository.count());

        MediaStatsDto media = new MediaStatsDto();
        media.setTotal(mediaRepository.count());

        List<RecentPostDto> recentPosts = blogRepository.findTop5ByOrderByUpdatedAtDesc()
                .stream()
                .map(this::toRecentPostDto)
                .collect(Collectors.toList());

        List<RecentProjectDto> recentProjects = projectRepository.findTop5ByOrderByUpdatedAtDesc()
                .stream()
                .map(this::toRecentProjectDto)
                .collect(Collectors.toList());

        DashboardStatsResponse response = new DashboardStatsResponse();
        response.setPosts(posts);
        response.setProjects(projects);
        response.setMedia(media);
        response.setRecentPosts(recentPosts);
        response.setRecentProjects(recentProjects);

        return response;
    }

    private RecentPostDto toRecentPostDto(Blog blog) {
        RecentPostDto dto = new RecentPostDto();
        dto.setId(blog.getId());
        dto.setTitle(blog.getTitle());
        dto.setSlug(blog.getSlug());
        dto.setStatus(blog.getStatus());
        dto.setCreatedAt(blog.getCreatedAt());
        dto.setUpdatedAt(blog.getUpdatedAt());
        dto.setPublishedAt(blog.getPublishedAt());
        return dto;
    }

    private RecentProjectDto toRecentProjectDto(Project project) {
        RecentProjectDto dto = new RecentProjectDto();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setSlug(project.getSlug());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());
        return dto;
    }
}
