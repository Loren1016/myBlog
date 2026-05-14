package com.myblog.service;

import com.myblog.dto.dashboard.DashboardStatsResponse;
import com.myblog.entity.Blog;
import com.myblog.entity.Project;
import com.myblog.repository.BlogRepository;
import com.myblog.repository.MediaRepository;
import com.myblog.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private BlogRepository blogRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private MediaRepository mediaRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(blogRepository, projectRepository, mediaRepository);
    }

    @Test
    void getStats_shouldReturnCorrectCounts() {
        when(blogRepository.count()).thenReturn(10L);
        when(blogRepository.countByStatus("published")).thenReturn(7L);
        when(blogRepository.countByStatus("draft")).thenReturn(3L);
        when(projectRepository.count()).thenReturn(5L);
        when(mediaRepository.count()).thenReturn(8L);

        Blog blog1 = createBlog("1", "title1", "slug-1", "published");
        Blog blog2 = createBlog("2", "title2", "slug-2", "draft");
        when(blogRepository.findTop5ByOrderByUpdatedAtDesc()).thenReturn(List.of(blog1, blog2));

        Project proj1 = createProject("p1", "proj1", "proj-1");
        when(projectRepository.findTop5ByOrderByUpdatedAtDesc()).thenReturn(List.of(proj1));

        DashboardStatsResponse stats = dashboardService.getStats();

        // posts stats
        assertEquals(10L, stats.getPosts().getTotal());
        assertEquals(7L, stats.getPosts().getPublished());
        assertEquals(3L, stats.getPosts().getDraft());

        // projects stats
        assertEquals(5L, stats.getProjects().getTotal());

        // media stats
        assertEquals(8L, stats.getMedia().getTotal());

        // recent posts
        assertEquals(2, stats.getRecentPosts().size());
        assertEquals("title1", stats.getRecentPosts().get(0).getTitle());
        assertEquals("published", stats.getRecentPosts().get(0).getStatus());
        assertEquals("title2", stats.getRecentPosts().get(1).getTitle());

        // recent projects
        assertEquals(1, stats.getRecentProjects().size());
        assertEquals("proj1", stats.getRecentProjects().get(0).getName());
    }

    @Test
    void getStats_shouldReturnZerosAndEmptyListsWhenDatabaseIsEmpty() {
        when(blogRepository.count()).thenReturn(0L);
        when(blogRepository.countByStatus("published")).thenReturn(0L);
        when(blogRepository.countByStatus("draft")).thenReturn(0L);
        when(projectRepository.count()).thenReturn(0L);
        when(mediaRepository.count()).thenReturn(0L);
        when(blogRepository.findTop5ByOrderByUpdatedAtDesc()).thenReturn(Collections.emptyList());
        when(projectRepository.findTop5ByOrderByUpdatedAtDesc()).thenReturn(Collections.emptyList());

        DashboardStatsResponse stats = dashboardService.getStats();

        assertEquals(0L, stats.getPosts().getTotal());
        assertEquals(0L, stats.getPosts().getPublished());
        assertEquals(0L, stats.getPosts().getDraft());
        assertEquals(0L, stats.getProjects().getTotal());
        assertEquals(0L, stats.getMedia().getTotal());
        assertTrue(stats.getRecentPosts().isEmpty());
        assertTrue(stats.getRecentProjects().isEmpty());
    }

    @Test
    void getStats_shouldQueryCountByStatusCorrectly() {
        when(blogRepository.count()).thenReturn(0L);
        when(blogRepository.countByStatus("published")).thenReturn(0L);
        when(blogRepository.countByStatus("draft")).thenReturn(0L);
        when(projectRepository.count()).thenReturn(0L);
        when(mediaRepository.count()).thenReturn(0L);
        when(blogRepository.findTop5ByOrderByUpdatedAtDesc()).thenReturn(Collections.emptyList());
        when(projectRepository.findTop5ByOrderByUpdatedAtDesc()).thenReturn(Collections.emptyList());

        dashboardService.getStats();

        verify(blogRepository).countByStatus("published");
        verify(blogRepository).countByStatus("draft");
    }

    private Blog createBlog(String id, String title, String slug, String status) {
        Blog blog = new Blog();
        blog.setId(id);
        blog.setTitle(title);
        blog.setSlug(slug);
        blog.setStatus(status);
        blog.setCreatedAt(LocalDateTime.now());
        blog.setUpdatedAt(LocalDateTime.now());
        blog.setPublishedAt(status.equals("published") ? LocalDateTime.now() : null);
        return blog;
    }

    private Project createProject(String id, String name, String slug) {
        Project project = new Project();
        project.setId(id);
        project.setName(name);
        project.setSlug(slug);
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        return project;
    }
}
