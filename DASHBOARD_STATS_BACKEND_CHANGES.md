# Dashboard Stats Backend Changes

## Summary

新增管理后台仪表盘统计接口 `GET /api/admin/dashboard/stats`，返回博客（posts）、作品（projects）、媒体（media）的计数以及最近更新的条目列表。数据全部来自数据库真实查询，无 mock/硬编码。

接口受 `/api/admin/**` 的 JWT 保护（无需修改 SecurityConfig）。

## Changed Files

| 操作 | 文件 |
|------|------|
| 新增 | `backend/src/main/java/com/myblog/dto/dashboard/PostStatsDto.java` |
| 新增 | `backend/src/main/java/com/myblog/dto/dashboard/ProjectStatsDto.java` |
| 新增 | `backend/src/main/java/com/myblog/dto/dashboard/MediaStatsDto.java` |
| 新增 | `backend/src/main/java/com/myblog/dto/dashboard/RecentPostDto.java` |
| 新增 | `backend/src/main/java/com/myblog/dto/dashboard/RecentProjectDto.java` |
| 新增 | `backend/src/main/java/com/myblog/dto/dashboard/DashboardStatsResponse.java` |
| 新增 | `backend/src/main/java/com/myblog/service/DashboardService.java` |
| 新增 | `backend/src/main/java/com/myblog/controller/AdminDashboardController.java` |
| 修改 | `backend/src/main/java/com/myblog/repository/BlogRepository.java` |
| 修改 | `backend/src/main/java/com/myblog/repository/ProjectRepository.java` |
| 修改 | `backend/pom.xml` (added `spring-security-test` dependency) |
| 新增 | `backend/src/test/java/com/myblog/service/DashboardServiceTest.java` |
| 新增 | `backend/src/test/java/com/myblog/controller/AdminDashboardControllerTest.java` |

## API

```
GET /api/admin/dashboard/stats
Authorization: Bearer <token>
```

**Response (200):**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "posts": {
      "total": 10,
      "published": 7,
      "draft": 3
    },
    "projects": {
      "total": 5
    },
    "media": {
      "total": 8
    },
    "recentPosts": [
      {
        "id": "uuid",
        "title": "文章标题",
        "slug": "post-slug",
        "status": "published",
        "createdAt": "2026-05-14T12:00:00",
        "updatedAt": "2026-05-14T12:00:00",
        "publishedAt": "2026-05-14T12:00:00"
      }
    ],
    "recentProjects": [
      {
        "id": "uuid",
        "name": "作品名称",
        "slug": "project-slug",
        "createdAt": "2026-05-14T12:00:00",
        "updatedAt": "2026-05-14T12:00:00"
      }
    ]
  }
}
```

**Response (401, unauthenticated):**

```json
{
  "code": 401,
  "message": "未登录或登录已过期",
  "data": null
}
```

## Full Code

### `backend/src/main/java/com/myblog/dto/dashboard/PostStatsDto.java`

```java
package com.myblog.dto.dashboard;

public class PostStatsDto {

    private long total;
    private long published;
    private long draft;

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public long getPublished() { return published; }
    public void setPublished(long published) { this.published = published; }

    public long getDraft() { return draft; }
    public void setDraft(long draft) { this.draft = draft; }
}
```

### `backend/src/main/java/com/myblog/dto/dashboard/ProjectStatsDto.java`

```java
package com.myblog.dto.dashboard;

public class ProjectStatsDto {

    private long total;

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
}
```

### `backend/src/main/java/com/myblog/dto/dashboard/MediaStatsDto.java`

```java
package com.myblog.dto.dashboard;

public class MediaStatsDto {

    private long total;

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
}
```

### `backend/src/main/java/com/myblog/dto/dashboard/RecentPostDto.java`

```java
package com.myblog.dto.dashboard;

import java.time.LocalDateTime;

public class RecentPostDto {

    private String id;
    private String title;
    private String slug;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
}
```

### `backend/src/main/java/com/myblog/dto/dashboard/RecentProjectDto.java`

```java
package com.myblog.dto.dashboard;

import java.time.LocalDateTime;

public class RecentProjectDto {

    private String id;
    private String name;
    private String slug;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
```

### `backend/src/main/java/com/myblog/dto/dashboard/DashboardStatsResponse.java`

```java
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
```

### `backend/src/main/java/com/myblog/service/DashboardService.java`

```java
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
```

### `backend/src/main/java/com/myblog/controller/AdminDashboardController.java`

```java
package com.myblog.controller;

import com.myblog.dto.ApiResponse;
import com.myblog.dto.dashboard.DashboardStatsResponse;
import com.myblog.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ApiResponse<DashboardStatsResponse> stats() {
        return ApiResponse.success(dashboardService.getStats());
    }
}
```

### `backend/src/main/java/com/myblog/repository/BlogRepository.java` (modified)

```java
package com.myblog.repository;

import com.myblog.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogRepository extends JpaRepository<Blog, String> {
    Optional<Blog> findBySlug(String slug);
    Page<Blog> findByStatus(String status, Pageable pageable);
    long countByStatus(String status);              // NEW
    List<Blog> findTop5ByOrderByUpdatedAtDesc();    // NEW
}
```

### `backend/src/main/java/com/myblog/repository/ProjectRepository.java` (modified)

```java
package com.myblog.repository;

import com.myblog.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {
    Optional<Project> findBySlug(String slug);
    List<Project> findTop5ByOrderByUpdatedAtDesc();  // NEW
}
```

## Verification

### Test Command

```bash
cd backend
mvn test
```

### Test Result

```
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

- `DashboardServiceTest` (3 tests): 验证正常统计数据、空数据库返回 0+空数组、正确调用 countByStatus
- `AdminDashboardControllerTest` (2 tests): 验证响应结构正确、空数据库返回 0
- `MyBlogApplicationTests` (1 test): 已存在的基础上下文加载测试

### 安全验证 (手动)

未认证访问返回 401:
```bash
curl -s http://localhost:8080/api/admin/dashboard/stats
# → {"code":401,"message":"未登录或登录已过期","data":null}
```

## Notes for Frontend

1. **Project 字段名**: `recentProjects` 中作品名称字段为 `name`（非 `title`），与现有 Project 实体保持一致
2. **Blog 状态**: `status` 字段值为 `"published"` 或 `"draft"`
3. **最近条数**: 固定最近 5 条，按 `updatedAt` 倒序
4. **认证**: 需要 Bearer Token，login 后从 localStorage 读取 `admin_access_token`
