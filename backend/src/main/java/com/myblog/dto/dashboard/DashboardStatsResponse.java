package com.myblog.dto.dashboard;

import java.util.List;

public class DashboardStatsResponse {

    private PostStatsDto posts;
    private ProjectStatsDto projects;
    private MediaStatsDto media;
    private List<RecentPostDto> recentPosts;
    private List<RecentProjectDto> recentProjects;

    public PostStatsDto getPosts() { return posts; }
    public void setPosts(PostStatsDto posts) { this.posts = posts; }

    public ProjectStatsDto getProjects() { return projects; }
    public void setProjects(ProjectStatsDto projects) { this.projects = projects; }

    public MediaStatsDto getMedia() { return media; }
    public void setMedia(MediaStatsDto media) { this.media = media; }

    public List<RecentPostDto> getRecentPosts() { return recentPosts; }
    public void setRecentPosts(List<RecentPostDto> recentPosts) { this.recentPosts = recentPosts; }

    public List<RecentProjectDto> getRecentProjects() { return recentProjects; }
    public void setRecentProjects(List<RecentProjectDto> recentProjects) { this.recentProjects = recentProjects; }
}
