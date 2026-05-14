package com.myblog.controller;

import com.myblog.dto.dashboard.*;
import com.myblog.security.JwtAuthenticationFilter;
import com.myblog.security.JwtProvider;
import com.myblog.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminDashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtProvider jwtProvider;

    @Test
    void stats_shouldReturn200_withCorrectStructure() throws Exception {
        DashboardStatsResponse mockResponse = createMockResponse();
        when(dashboardService.getStats()).thenReturn(mockResponse);

        mockMvc.perform(get("/api/admin/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.posts.total").value(5))
                .andExpect(jsonPath("$.data.posts.published").value(3))
                .andExpect(jsonPath("$.data.posts.draft").value(2))
                .andExpect(jsonPath("$.data.projects.total").value(4))
                .andExpect(jsonPath("$.data.media.total").value(10))
                .andExpect(jsonPath("$.data.recentPosts.length()").value(1))
                .andExpect(jsonPath("$.data.recentPosts[0].title").value("Test Post"))
                .andExpect(jsonPath("$.data.recentProjects.length()").value(1))
                .andExpect(jsonPath("$.data.recentProjects[0].name").value("Test Project"));
    }

    @Test
    void stats_shouldReturnZeros_whenDatabaseIsEmpty() throws Exception {
        DashboardStatsResponse emptyResponse = createEmptyResponse();
        when(dashboardService.getStats()).thenReturn(emptyResponse);

        mockMvc.perform(get("/api/admin/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.posts.total").value(0))
                .andExpect(jsonPath("$.data.posts.published").value(0))
                .andExpect(jsonPath("$.data.posts.draft").value(0))
                .andExpect(jsonPath("$.data.projects.total").value(0))
                .andExpect(jsonPath("$.data.media.total").value(0))
                .andExpect(jsonPath("$.data.recentPosts.length()").value(0))
                .andExpect(jsonPath("$.data.recentProjects.length()").value(0));
    }

    private DashboardStatsResponse createMockResponse() {
        PostStatsDto posts = new PostStatsDto();
        posts.setTotal(5);
        posts.setPublished(3);
        posts.setDraft(2);

        ProjectStatsDto projects = new ProjectStatsDto();
        projects.setTotal(4);

        MediaStatsDto media = new MediaStatsDto();
        media.setTotal(10);

        RecentPostDto post = new RecentPostDto();
        post.setId("1");
        post.setTitle("Test Post");
        post.setSlug("test-post");
        post.setStatus("published");
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        post.setPublishedAt(LocalDateTime.now());

        RecentProjectDto project = new RecentProjectDto();
        project.setId("p1");
        project.setName("Test Project");
        project.setSlug("test-project");
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        DashboardStatsResponse response = new DashboardStatsResponse();
        response.setPosts(posts);
        response.setProjects(projects);
        response.setMedia(media);
        response.setRecentPosts(List.of(post));
        response.setRecentProjects(List.of(project));

        return response;
    }

    private DashboardStatsResponse createEmptyResponse() {
        PostStatsDto posts = new PostStatsDto();
        posts.setTotal(0);
        posts.setPublished(0);
        posts.setDraft(0);

        ProjectStatsDto projects = new ProjectStatsDto();
        projects.setTotal(0);

        MediaStatsDto media = new MediaStatsDto();
        media.setTotal(0);

        DashboardStatsResponse response = new DashboardStatsResponse();
        response.setPosts(posts);
        response.setProjects(projects);
        response.setMedia(media);
        response.setRecentPosts(List.of());
        response.setRecentProjects(List.of());

        return response;
    }
}
