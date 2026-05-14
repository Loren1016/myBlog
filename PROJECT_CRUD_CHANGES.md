# Project Admin CRUD — 完整变更记录

> 更新日期: 2026-05-14（后端 + 前端作品管理 CRUD 全部实现）

---

## 变更概览

### 后端（3 个文件）

| 文件 | 操作 | 说明 |
|------|------|------|
| `backend/.../entity/Project.java` | 修改 | 添加 `@NotBlank` / `@Size` 校验注解 |
| `backend/.../service/ProjectService.java` | 修改 | 新增 `findById` / `findAllPaginated`；修复 `create()` 服务端字段控制；分页参数限制 |
| `backend/.../controller/AdminProjectController.java` | 新增 | 作品管理端 CRUD 控制器（5 个接口） |

### 前端（6 个文件）

| 文件 | 操作 | 说明 |
|------|------|------|
| `frontend/src/lib/api.ts` | 修改 | 新增 `AdminProject` / `PaginatedProjects` / `ProjectInput` 类型 + 5 个 API 方法 |
| `frontend/src/app/admin/page.tsx` | 修改 | 仪表盘新增「作品管理」「新建作品」入口；`<a>` 改为 `<Link>` |
| `frontend/src/app/admin/projects/ProjectEditor.tsx` | 新增 | 作品编辑器（新建/编辑共用） |
| `frontend/src/app/admin/projects/page.tsx` | 新增 | 作品管理列表页 |
| `frontend/src/app/admin/projects/new/page.tsx` | 新增 | 新建作品页 |
| `frontend/src/app/admin/projects/[id]/page.tsx` | 新增 | 编辑作品页 |

---

## 前端数据流

```
/admin (仪表盘) → 点击「作品管理」→ /admin/projects (列表)
                                    → 点击「新建作品」→ /admin/projects/new (ProjectEditor)
                                    → 点击某作品「编辑」→ /admin/projects/[id] (ProjectEditor)

ProjectEditor (新建模式):
  表单输入 → buildInput() → adminCreateProject(token, data)
           → POST /api/admin/projects → 返回新 Project → router.replace(/admin/projects/{newId})

ProjectEditor (编辑模式):
  useParams() 获取 id → adminGetProject(token, id) → 填充表单
                     → 保存 → buildInput() → adminUpdateProject(token, id, data)
                            → PUT /api/admin/projects/{id}

作品列表删除:
  点击「删除」→ 二次确认 → adminDeleteProject(token, id)
                         → DELETE /api/admin/projects/{id} → 本地 setProjects 移除
```

### screenshots / techStack 字段转换

后端 Project 实体中这两个字段是 `String`（JSON 文本如 `["React","Java"]`），前端 `types/project.ts` 类型为 `string[]`。编辑器采用兼容方案：

- **显示时**：`jsonArrayStringToCommaText(value)` 先尝试 `JSON.parse`，失败则按原始文本或数组处理，最终输出逗号分隔文本
- **提交时**：`commaTextToJsonArrayString(text)` 将逗号分隔输入转换为 JSON 数组字符串 `'["a","b"]'`

---

## 后端改进说明

1. **实体验证** — `Project.java` 的 `name` 和 `slug` 添加 `@NotBlank`，`tagline` 添加 `@Size(max=200)`；`GlobalExceptionHandler` 已有 `MethodArgumentNotValidException` 处理返回 400
2. **创建时服务端字段控制** — `ProjectService.create()` 强制将 `id`、`createdAt`、`updatedAt` 置为 null，由 `@PrePersist` 自动生成
3. **分页参数限制** — `findAllPaginated()` 限制 `page >= 0`、`1 <= size <= 100`

---

## 全部修改文件完整代码

### backend/src/main/java/com/myblog/entity/Project.java

```java
package com.myblog.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "project")
public class Project {

    @Id
    private String id;

    @NotBlank(message = "作品名称不能为空")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Slug 不能为空")
    @Column(nullable = false, unique = true)
    private String slug;

    @Size(max = 200, message = "简介不能超过200个字符")
    @Column(length = 200)
    private String tagline;

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    private String coverImage;

    @Column(columnDefinition = "JSON")
    private String screenshots;

    @Column(columnDefinition = "JSON")
    private String techStack;

    private String demoUrl;

    private String sourceUrl;

    private int priority = 0;

    private String role;

    private LocalDateTime completedAt;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    public Project() {}

    @PrePersist
    public void onCreate() {
        if (id == null) id = java.util.UUID.randomUUID().toString();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getTagline() { return tagline; }
    public void setTagline(String tagline) { this.tagline = tagline; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    public String getScreenshots() { return screenshots; }
    public void setScreenshots(String screenshots) { this.screenshots = screenshots; }
    public String getTechStack() { return techStack; }
    public void setTechStack(String techStack) { this.techStack = techStack; }
    public String getDemoUrl() { return demoUrl; }
    public void setDemoUrl(String demoUrl) { this.demoUrl = demoUrl; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
```

### backend/src/main/java/com/myblog/service/ProjectService.java

```java
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
```

### backend/src/main/java/com/myblog/controller/AdminProjectController.java

```java
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
```

### frontend/src/lib/api.ts

```ts
const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

async function request<T>(
  endpoint: string,
  options?: RequestInit
): Promise<ApiResponse<T>> {
  const url = `${API_BASE}${endpoint}`;
  const res = await fetch(url, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...options?.headers,
    },
  });

  if (!res.ok) {
    const error = await res.json().catch(() => ({}));
    throw new Error(error.message || `Request failed: ${res.status}`);
  }

  return res.json();
}

/* ---- Blog (public) ---- */

export interface Blog {
  id: string;
  title: string;
  slug: string;
  summary: string | null;
  content: string;
  coverImage: string | null;
  tags: string | null;
  category: string | null;
  status: "draft" | "published";
  publishedAt: string | null;
  createdAt: string;
  updatedAt: string;
  viewCount: number;
}

export interface PaginatedPosts {
  content: Blog[];
  totalElements: number;
  totalPages: number;
  page: number;
}

export async function getPublishedBlogs(page = 0) {
  return request<PaginatedPosts>(`/posts?page=${page}&size=50`);
}

export async function getPublishedBlogBySlug(slug: string) {
  return request<Blog>(`/posts/${slug}`);
}

/* ---- Projects (public) ---- */

export async function getProjects(params?: {
  page?: number;
  tech?: string;
}) {
  const query = new URLSearchParams();
  if (params?.page) query.set("page", String(params.page));
  if (params?.tech) query.set("tech", params.tech);

  const qs = query.toString();
  return request(`/projects${qs ? `?${qs}` : ""}`);
}

export async function getProjectBySlug(slug: string) {
  return request(`/projects/${slug}`);
}

/* ---- Auth ---- */

export interface AdminUser {
  id: string;
  email: string;
  displayName: string;
  avatar: string | null;
  role: string;
}

export interface LoginResult {
  accessToken: string;
  refreshToken: string;
  user: AdminUser;
}

export async function adminLogin(email: string, password: string) {
  return request<LoginResult>("/auth/admin/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
}

export async function adminRefresh(refreshToken: string) {
  return request<LoginResult>("/auth/admin/refresh", {
    method: "POST",
    body: JSON.stringify({ refreshToken }),
  });
}

export async function adminLogout(token: string) {
  return request<null>("/auth/admin/logout", {
    method: "POST",
    headers: { Authorization: `Bearer ${token}` },
  });
}

export async function adminGetMe(token: string) {
  return request<AdminUser>("/auth/admin/me", {
    headers: { Authorization: `Bearer ${token}` },
  });
}

/* ---- Admin Blog CRUD ---- */

export interface BlogInput {
  title: string;
  slug: string;
  summary?: string;
  content?: string;
  coverImage?: string;
  tags?: string;
  category?: string;
  status?: string;
}

export async function adminListPosts(token: string, page = 0) {
  return request<PaginatedPosts>(`/admin/posts?page=${page}&size=50`, {
    headers: { Authorization: `Bearer ${token}` },
  });
}

export async function adminGetPost(token: string, id: string) {
  return request<Blog>(`/admin/posts/${id}`, {
    headers: { Authorization: `Bearer ${token}` },
  });
}

export async function adminCreatePost(token: string, data: BlogInput) {
  return request<Blog>("/admin/posts", {
    method: "POST",
    headers: { Authorization: `Bearer ${token}` },
    body: JSON.stringify(data),
  });
}

export async function adminUpdatePost(token: string, id: string, data: BlogInput) {
  return request<Blog>(`/admin/posts/${id}`, {
    method: "PUT",
    headers: { Authorization: `Bearer ${token}` },
    body: JSON.stringify(data),
  });
}

export async function adminDeletePost(token: string, id: string) {
  return request<null>(`/admin/posts/${id}`, {
    method: "DELETE",
    headers: { Authorization: `Bearer ${token}` },
  });
}

/* ---- Admin Project CRUD ---- */

export interface AdminProject {
  id: string;
  name: string;
  slug: string;
  tagline: string | null;
  description: string | null;
  coverImage: string | null;
  screenshots: string | null;
  techStack: string | null;
  demoUrl: string | null;
  sourceUrl: string | null;
  priority: number;
  role: string | null;
  completedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface PaginatedProjects {
  content: AdminProject[];
  totalElements: number;
  totalPages: number;
  page: number;
}

export interface ProjectInput {
  name: string;
  slug: string;
  tagline?: string;
  description?: string;
  coverImage?: string;
  screenshots?: string;
  techStack?: string;
  demoUrl?: string;
  sourceUrl?: string;
  priority?: number;
  role?: string;
  completedAt?: string;
}

export async function adminListProjects(token: string, page = 0) {
  return request<PaginatedProjects>(`/admin/projects?page=${page}&size=50`, {
    headers: { Authorization: `Bearer ${token}` },
  });
}

export async function adminGetProject(token: string, id: string) {
  return request<AdminProject>(`/admin/projects/${id}`, {
    headers: { Authorization: `Bearer ${token}` },
  });
}

export async function adminCreateProject(token: string, data: ProjectInput) {
  return request<AdminProject>("/admin/projects", {
    method: "POST",
    headers: { Authorization: `Bearer ${token}` },
    body: JSON.stringify(data),
  });
}

export async function adminUpdateProject(token: string, id: string, data: ProjectInput) {
  return request<AdminProject>(`/admin/projects/${id}`, {
    method: "PUT",
    headers: { Authorization: `Bearer ${token}` },
    body: JSON.stringify(data),
  });
}

export async function adminDeleteProject(token: string, id: string) {
  return request<null>(`/admin/projects/${id}`, {
    method: "DELETE",
    headers: { Authorization: `Bearer ${token}` },
  });
}
```

### frontend/src/app/admin/page.tsx

```tsx
"use client";

import Link from "next/link";
import { Container } from "@/components/layout/Container";
import { LoginForm } from "@/components/admin/LoginForm";
import { AuthGuard } from "@/components/admin/AuthGuard";
import type { AdminUser } from "@/lib/api";

export default function AdminPage() {
  return (
    <section className="py-section pb-section-lg">
      <Container>
        <AuthGuard
          fallback={<LoginSection />}
        >
          {({ user, onLogout }) => <Dashboard user={user} onLogout={onLogout} />}
        </AuthGuard>
      </Container>
    </section>
  );
}

function LoginSection() {
  return (
    <div className="max-w-4xl">
      <LoginForm onLoginSuccess={() => window.location.reload()} />
    </div>
  );
}

function Dashboard({ user, onLogout }: { user: AdminUser; onLogout: () => void }) {
  return (
    <div className="max-w-4xl">
      {/* Header */}
      <div className="flex items-start justify-between mb-4">
        <div>
          <p className="text-[11px] tracking-[0.2em] uppercase text-warm-gray mb-4 font-medium">
            管理
          </p>
          <h1 className="mb-2">仪表盘</h1>
        </div>
        <div className="text-right">
          <p className="text-sm text-charcoal font-medium">{user.displayName}</p>
          <p className="text-xs text-warm-gray mb-3">{user.email}</p>
          <button
            onClick={onLogout}
            className="text-[11px] tracking-[0.15em] uppercase text-warm-gray hover:text-charcoal link-underline transition-colors pb-0.5"
          >
            退出登录
          </button>
        </div>
      </div>
      <hr />

      {/* Stats */}
      <div className="mt-16 max-w-4xl">
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 mb-16">
          {[
            { label: "已发布文章", value: "—" },
            { label: "作品数量", value: "—" },
            { label: "媒体文件", value: "—" },
          ].map((stat) => (
            <div
              key={stat.label}
              className="border border-border p-8"
            >
              <p className="text-[11px] tracking-[0.15em] uppercase text-warm-gray mb-3">
                {stat.label}
              </p>
              <p className="text-4xl font-serif text-charcoal">{stat.value}</p>
            </div>
          ))}
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
          <Link
            href="/admin/posts"
            className="block border border-border p-10 hover:bg-cream-deep/50 transition-colors duration-300 group"
          >
            <h3 className="font-serif text-2xl font-normal mb-3 text-charcoal group-hover:text-charcoal/70 transition-colors">
              文章管理
            </h3>
            <p className="text-charcoal-soft text-sm mb-0">
              管理博客文章：新建、编辑、发布与删除。
            </p>
          </Link>
          <Link
            href="/admin/posts/new"
            className="block border border-border p-10 hover:bg-cream-deep/50 transition-colors duration-300 group"
          >
            <h3 className="font-serif text-2xl font-normal mb-3 text-charcoal group-hover:text-charcoal/70 transition-colors">
              新建文章
            </h3>
            <p className="text-charcoal-soft text-sm mb-0">
              撰写并发布一篇新博客文章。
            </p>
          </Link>
          <Link
            href="/admin/projects"
            className="block border border-border p-10 hover:bg-cream-deep/50 transition-colors duration-300 group"
          >
            <h3 className="font-serif text-2xl font-normal mb-3 text-charcoal group-hover:text-charcoal/70 transition-colors">
              作品管理
            </h3>
            <p className="text-charcoal-soft text-sm mb-0">
              管理作品项目：新建、编辑与删除。
            </p>
          </Link>
          <Link
            href="/admin/projects/new"
            className="block border border-border p-10 hover:bg-cream-deep/50 transition-colors duration-300 group"
          >
            <h3 className="font-serif text-2xl font-normal mb-3 text-charcoal group-hover:text-charcoal/70 transition-colors">
              新建作品
            </h3>
            <p className="text-charcoal-soft text-sm mb-0">
              创建一个新的作品项目。
            </p>
          </Link>
        </div>

        <p className="text-warm-gray text-sm italic mt-16 text-center">
          管理功能将在后续阶段完整实现。
        </p>
      </div>
    </div>
  );
}
```

### frontend/src/app/admin/projects/ProjectEditor.tsx

```tsx
"use client";

import { useEffect, useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { getAccessToken } from "@/lib/auth";
import {
  adminGetProject,
  adminCreateProject,
  adminUpdateProject,
  type ProjectInput,
} from "@/lib/api";

/* ---- helpers ---- */

function jsonArrayStringToCommaText(value?: string | string[] | null): string {
  if (!value) return "";
  if (Array.isArray(value)) return value.join(", ");
  try {
    const arr = JSON.parse(value);
    if (Array.isArray(arr)) return arr.join(", ");
  } catch {
    // not valid JSON, treat as literal text
  }
  return value;
}

function commaTextToJsonArrayString(text: string): string {
  const trimmed = text.trim();
  if (!trimmed) return "[]";
  const items = trimmed
    .split(",")
    .map((s) => s.trim())
    .filter((s) => s.length > 0);
  return JSON.stringify(items);
}

/* ---- component ---- */

interface Props {
  id?: string;
}

export function ProjectEditor({ id }: Props) {
  const router = useRouter();
  const isEdit = !!id;

  const [name, setName] = useState("");
  const [slug, setSlug] = useState("");
  const [tagline, setTagline] = useState("");
  const [description, setDescription] = useState("");
  const [coverImage, setCoverImage] = useState("");
  const [screenshotsText, setScreenshotsText] = useState("");
  const [techStackText, setTechStackText] = useState("");
  const [demoUrl, setDemoUrl] = useState("");
  const [sourceUrl, setSourceUrl] = useState("");
  const [priority, setPriority] = useState("0");
  const [role, setRole] = useState("");
  const [completedAt, setCompletedAt] = useState("");
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(isEdit);
  const [error, setError] = useState("");
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    if (!id) return;
    const token = getAccessToken();
    if (!token) return;

    adminGetProject(token, id)
      .then((res) => {
        const p = res.data;
        setName(p.name || "");
        setSlug(p.slug || "");
        setTagline(p.tagline || "");
        setDescription(p.description || "");
        setCoverImage(p.coverImage || "");
        setScreenshotsText(jsonArrayStringToCommaText(p.screenshots));
        setTechStackText(jsonArrayStringToCommaText(p.techStack));
        setDemoUrl(p.demoUrl || "");
        setSourceUrl(p.sourceUrl || "");
        setPriority(String(p.priority ?? 0));
        setRole(p.role || "");
        setCompletedAt(p.completedAt ? p.completedAt.substring(0, 10) : "");
      })
      .catch((err) => setError(err instanceof Error ? err.message : "加载失败"))
      .finally(() => setFetching(false));
  }, [id]);

  function buildInput(): ProjectInput {
    return {
      name: name.trim(),
      slug: slug.trim(),
      tagline: tagline.trim() || undefined,
      description: description || undefined,
      coverImage: coverImage.trim() || undefined,
      screenshots: commaTextToJsonArrayString(screenshotsText),
      techStack: commaTextToJsonArrayString(techStackText),
      demoUrl: demoUrl.trim() || undefined,
      sourceUrl: sourceUrl.trim() || undefined,
      priority: Number(priority) || 0,
      role: role.trim() || undefined,
      completedAt: completedAt ? `${completedAt}T00:00:00` : undefined,
    };
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError("");
    setSaved(false);

    if (!name.trim()) {
      setError("请填写作品名称");
      return;
    }
    if (!slug.trim()) {
      setError("请填写 Slug");
      return;
    }
    if (isNaN(Number(priority))) {
      setError("优先级必须是数字");
      return;
    }

    const token = getAccessToken();
    if (!token) {
      setError("未登录");
      return;
    }

    setLoading(true);
    try {
      const data = buildInput();
      if (isEdit) {
        await adminUpdateProject(token, id!, data);
      } else {
        const res = await adminCreateProject(token, data);
        const newId = res.data.id;
        router.replace(`/admin/projects/${newId}`);
      }
      setSaved(true);
      setTimeout(() => setSaved(false), 2000);
    } catch (err) {
      setError(err instanceof Error ? err.message : "保存失败");
    } finally {
      setLoading(false);
    }
  }

  if (fetching) {
    return (
      <div className="max-w-2xl">
        <p className="text-warm-gray text-sm italic">加载作品…</p>
      </div>
    );
  }

  return (
    <div className="max-w-2xl">
      <div className="flex items-center justify-between mb-4">
        <div>
          <p className="text-[11px] tracking-[0.2em] uppercase text-warm-gray mb-4 font-medium">
            管理
          </p>
          <h1 className="mb-1">{isEdit ? "编辑作品" : "新建作品"}</h1>
        </div>
        <Link
          href="/admin/projects"
          className="text-[11px] tracking-[0.15em] uppercase text-warm-gray hover:text-charcoal transition-colors"
        >
          &larr; 作品列表
        </Link>
      </div>
      <hr className="mb-12" />

      {error && (
        <div className="border border-red-200 bg-red-50/30 text-red-700 text-sm px-4 py-3 mb-8">
          {error}
        </div>
      )}

      {saved && (
        <div className="border border-sage-deep/30 bg-sage/10 text-sage-dark text-sm px-4 py-3 mb-8">
          已保存。
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-8">
        {/* Name */}
        <div>
          <label className="block text-[11px] tracking-[0.15em] uppercase text-warm-gray mb-2">
            作品名称 <span className="text-red-400">*</span>
          </label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="w-full bg-transparent border-b border-warm-gray-light px-1 py-2 text-charcoal text-lg font-serif focus:outline-none focus:border-charcoal/50 transition-colors"
            placeholder="作品名称"
          />
        </div>

        {/* Slug + Priority */}
        <div className="grid grid-cols-2 gap-6">
          <div>
            <label className="block text-[11px] tracking-[0.15em] uppercase text-warm-gray mb-2">
              Slug <span className="text-red-400">*</span>
            </label>
            <input
              type="text"
              value={slug}
              onChange={(e) => setSlug(e.target.value)}
              className="w-full bg-transparent border-b border-warm-gray-light px-1 py-2 text-charcoal text-sm focus:outline-none focus:border-charcoal/50 transition-colors"
              placeholder="my-project-slug"
            />
          </div>
          <div>
            <label className="block text-[11px] tracking-[0.15em] uppercase text-warm-gray mb-2">
              优先级
            </label>
            <input
              type="number"
              value={priority}
              onChange={(e) => setPriority(e.target.value)}
              className="w-full bg-transparent border-b border-warm-gray-light px-1 py-2 text-charcoal text-sm focus:outline-none focus:border-charcoal/50 transition-colors"
              placeholder="0"
            />
          </div>
        </div>

        {/* Tagline */}
        <div>
          <label className="block text-[11px] tracking-[0.15em] uppercase text-warm-gray mb-2">
            一句话简介
          </label>
          <input
            type="text"
            value={tagline}
            onChange={(e) => setTagline(e.target.value)}
            className="w-full bg-transparent border-b border-warm-gray-light px-1 py-2 text-charcoal text-sm focus:outline-none focus:border-charcoal/50 transition-colors"
            placeholder="一句话介绍这个作品…"
          />
        </div>

        {/* CoverImage */}
        <div>
          <label className="block text-[11px] tracking-[0.15em] uppercase text-warm-gray mb-2">
            封面图 URL
          </label>
          <input
            type="text"
            value={coverImage}
            onChange={(e) => setCoverImage(e.target.value)}
            className="w-full bg-transparent border-b border-warm-gray-light px-1 py-2 text-charcoal text-sm focus:outline-none focus:border-charcoal/50 transition-colors"
            placeholder="/images/project-cover.jpg"
          />
        </div>

        {/* Description */}
        <div>
          <label className="block text-[11px] tracking-[0.15em] uppercase text-warm-gray mb-2">
            详细介绍（Markdown）
          </label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={16}
            className="w-full bg-cream-deep/50 border border-warm-gray-light px-4 py-4 text-charcoal text-sm font-mono focus:outline-none focus:border-charcoal/30 transition-colors resize-y"
            placeholder="用 Markdown 编写作品详细介绍…"
          />
        </div>

        {/* TechStack + Screenshots */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
          <div>
            <label className="block text-[11px] tracking-[0.15em] uppercase text-warm-gray mb-2">
              技术栈（逗号分隔）
            </label>
            <input
              type="text"
              value={techStackText}
              onChange={(e) => setTechStackText(e.target.value)}
              className="w-full bg-transparent border-b border-warm-gray-light px-1 py-2 text-charcoal text-sm focus:outline-none focus:border-charcoal/50 transition-colors"
              placeholder="React, Spring Boot, MySQL"
            />
          </div>
          <div>
            <label className="block text-[11px] tracking-[0.15em] uppercase text-warm-gray mb-2">
              截图 URL（逗号分隔）
            </label>
            <input
              type="text"
              value={screenshotsText}
              onChange={(e) => setScreenshotsText(e.target.value)}
              className="w-full bg-transparent border-b border-warm-gray-light px-1 py-2 text-charcoal text-sm focus:outline-none focus:border-charcoal/50 transition-colors"
              placeholder="/images/screen-1.jpg, /images/screen-2.jpg"
            />
          </div>
        </div>

        {/* DemoUrl + SourceUrl */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
          <div>
            <label className="block text-[11px] tracking-[0.15em] uppercase text-warm-gray mb-2">
              演示链接
            </label>
            <input
              type="text"
              value={demoUrl}
              onChange={(e) => setDemoUrl(e.target.value)}
              className="w-full bg-transparent border-b border-warm-gray-light px-1 py-2 text-charcoal text-sm focus:outline-none focus:border-charcoal/50 transition-colors"
              placeholder="https://example.com"
            />
          </div>
          <div>
            <label className="block text-[11px] tracking-[0.15em] uppercase text-warm-gray mb-2">
              源码链接
            </label>
            <input
              type="text"
              value={sourceUrl}
              onChange={(e) => setSourceUrl(e.target.value)}
              className="w-full bg-transparent border-b border-warm-gray-light px-1 py-2 text-charcoal text-sm focus:outline-none focus:border-charcoal/50 transition-colors"
              placeholder="https://github.com/..."
            />
          </div>
        </div>

        {/* Role + CompletedAt */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
          <div>
            <label className="block text-[11px] tracking-[0.15em] uppercase text-warm-gray mb-2">
              我的角色
            </label>
            <input
              type="text"
              value={role}
              onChange={(e) => setRole(e.target.value)}
              className="w-full bg-transparent border-b border-warm-gray-light px-1 py-2 text-charcoal text-sm focus:outline-none focus:border-charcoal/50 transition-colors"
              placeholder="独立全栈开发"
            />
          </div>
          <div>
            <label className="block text-[11px] tracking-[0.15em] uppercase text-warm-gray mb-2">
              完成日期
            </label>
            <input
              type="date"
              value={completedAt}
              onChange={(e) => setCompletedAt(e.target.value)}
              className="w-full bg-transparent border-b border-warm-gray-light px-1 py-2 text-charcoal text-sm focus:outline-none focus:border-charcoal/50 transition-colors"
            />
          </div>
        </div>

        {/* Submit */}
        <div className="flex items-center gap-4 pt-4">
          <button
            type="submit"
            disabled={loading}
            className="inline-flex items-center gap-2 text-[11px] tracking-[0.2em] uppercase text-cream bg-charcoal px-4 py-2 hover:bg-charcoal/90 transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
          >
            {isEdit ? "保存" : "创建"}
          </button>
        </div>
      </form>
    </div>
  );
}
```

### frontend/src/app/admin/projects/page.tsx

```tsx
"use client";

import { useEffect, useState } from "react";
import { Container } from "@/components/layout/Container";
import { AuthGuard } from "@/components/admin/AuthGuard";
import { getAccessToken } from "@/lib/auth";
import {
  adminListProjects,
  adminDeleteProject,
  type AdminProject,
} from "@/lib/api";
import Link from "next/link";

export default function AdminProjectsPage() {
  return (
    <section className="py-section pb-section-lg">
      <Container>
        <AuthGuard fallback={<LoginPrompt />}>
          {({ user }) => <ProjectList userDisplayName={user.displayName} />}
        </AuthGuard>
      </Container>
    </section>
  );
}

function LoginPrompt() {
  return (
    <div className="max-w-md">
      <p className="text-[11px] tracking-[0.2em] uppercase text-warm-gray mb-4 font-medium">
        管理
      </p>
      <h1 className="mb-3">作品管理</h1>
      <p className="text-charcoal-soft text-sm">请先登录以管理作品。</p>
      <Link
        href="/admin"
        className="inline-block mt-8 text-[11px] tracking-[0.2em] uppercase text-warm-gray link-underline hover:text-charcoal transition-colors"
      >
        &larr; 返回登录
      </Link>
    </div>
  );
}

function ProjectList({ userDisplayName }: { userDisplayName: string }) {
  const [projects, setProjects] = useState<AdminProject[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [deleteId, setDeleteId] = useState<string | null>(null);

  async function loadProjects() {
    const token = getAccessToken();
    if (!token) return;
    setLoading(true);
    try {
      const res = await adminListProjects(token);
      setProjects(res.data.content);
    } catch (err) {
      setError(err instanceof Error ? err.message : "加载失败");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadProjects();
  }, []);

  async function handleDelete(id: string) {
    const token = getAccessToken();
    if (!token) return;
    try {
      await adminDeleteProject(token, id);
      setProjects((prev) => prev.filter((p) => p.id !== id));
      setDeleteId(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "删除失败");
    }
  }

  function techStackPreview(raw: string | null): string {
    if (!raw) return "—";
    try {
      const arr = JSON.parse(raw);
      if (Array.isArray(arr)) return arr.join(" · ");
    } catch {
      // ignore
    }
    return raw;
  }

  return (
    <div className="max-w-4xl">
      <div className="flex items-center justify-between mb-4">
        <div>
          <p className="text-[11px] tracking-[0.2em] uppercase text-warm-gray mb-4 font-medium">
            管理
          </p>
          <h1 className="mb-1">作品管理</h1>
          <p className="text-sm text-charcoal-soft">
            共 {projects.length} 个作品 · {userDisplayName}
          </p>
        </div>
        <div className="flex items-center gap-4">
          <Link
            href="/admin/projects/new"
            className="text-[11px] tracking-[0.2em] uppercase text-charcoal border-b border-charcoal/40 pb-1 hover:border-charcoal/80 transition-colors"
          >
            新建作品
          </Link>
          <Link
            href="/admin"
            className="text-[11px] tracking-[0.15em] uppercase text-warm-gray hover:text-charcoal transition-colors"
          >
            仪表盘
          </Link>
        </div>
      </div>
      <hr className="mb-10" />

      {error && (
        <div className="border border-red-200 bg-red-50/30 text-red-700 text-sm px-4 py-3 mb-8">
          {error}
        </div>
      )}

      {loading ? (
        <p className="text-warm-gray text-sm italic">加载中…</p>
      ) : projects.length === 0 ? (
        <div className="text-center py-20">
          <p className="text-warm-gray text-sm italic mb-8">还没有作品。</p>
          <Link
            href="/admin/projects/new"
            className="text-[11px] tracking-[0.2em] uppercase text-charcoal border-b border-charcoal/40 pb-1 hover:border-charcoal/80 transition-colors"
          >
            新建第一个作品
          </Link>
        </div>
      ) : (
        <div>
          {projects.map((proj) => (
            <div
              key={proj.id}
              className="flex items-center justify-between py-5 border-b border-border first:pt-0 last:border-b-0"
            >
              <div className="min-w-0 flex-1">
                <div className="flex items-center gap-3 mb-1">
                  <Link
                    href={`/admin/projects/${proj.id}`}
                    className="font-serif text-lg text-charcoal hover:text-charcoal/60 transition-colors truncate"
                  >
                    {proj.name || "无标题"}
                  </Link>
                  {proj.priority > 0 && (
                    <span className="text-[10px] tracking-[0.18em] uppercase text-sage-deep bg-sage/20 px-2 py-0.5">
                      置顶 {proj.priority}
                    </span>
                  )}
                </div>
                <p className="text-xs text-warm-gray truncate">
                  {proj.slug}
                  {proj.tagline && <> &middot; {proj.tagline}</>}
                </p>
                <p className="text-[10px] text-warm-gray mt-0.5">
                  {techStackPreview(proj.techStack)}
                </p>
              </div>
              <div className="flex items-center gap-4 ml-6">
                <Link
                  href={`/admin/projects/${proj.id}`}
                  className="text-[11px] tracking-[0.15em] uppercase text-warm-gray hover:text-charcoal transition-colors"
                >
                  编辑
                </Link>
                {deleteId === proj.id ? (
                  <span className="flex items-center gap-2">
                    <button
                      onClick={() => handleDelete(proj.id)}
                      className="text-[11px] tracking-[0.15em] uppercase text-red-700 hover:text-red-900 transition-colors"
                    >
                      确认
                    </button>
                    <button
                      onClick={() => setDeleteId(null)}
                      className="text-[11px] tracking-[0.15em] uppercase text-warm-gray hover:text-charcoal transition-colors"
                    >
                      取消
                    </button>
                  </span>
                ) : (
                  <button
                    onClick={() => setDeleteId(proj.id)}
                    className="text-[11px] tracking-[0.15em] uppercase text-warm-gray hover:text-red-700 transition-colors"
                  >
                    删除
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
```

### frontend/src/app/admin/projects/new/page.tsx

```tsx
"use client";

import { Container } from "@/components/layout/Container";
import { AuthGuard } from "@/components/admin/AuthGuard";
import { ProjectEditor } from "../ProjectEditor";

export default function NewProjectPage() {
  return (
    <section className="py-section pb-section-lg">
      <Container>
        <AuthGuard fallback={<LoginPrompt />}>
          {() => <ProjectEditor />}
        </AuthGuard>
      </Container>
    </section>
  );
}

function LoginPrompt() {
  return (
    <div className="max-w-md">
      <p className="text-[11px] tracking-[0.2em] uppercase text-warm-gray mb-4 font-medium">
        管理
      </p>
      <h1 className="mb-3">新建作品</h1>
      <p className="text-charcoal-soft text-sm">请先登录。</p>
    </div>
  );
}
```

### frontend/src/app/admin/projects/[id]/page.tsx

```tsx
"use client";

import { useParams } from "next/navigation";
import { Container } from "@/components/layout/Container";
import { AuthGuard } from "@/components/admin/AuthGuard";
import { ProjectEditor } from "../ProjectEditor";

export default function EditProjectPage() {
  const params = useParams();
  const id = params.id as string;

  return (
    <section className="py-section pb-section-lg">
      <Container>
        <AuthGuard fallback={<LoginPrompt />}>
          {() => <ProjectEditor id={id} />}
        </AuthGuard>
      </Container>
    </section>
  );
}

function LoginPrompt() {
  return (
    <div className="max-w-md">
      <p className="text-[11px] tracking-[0.2em] uppercase text-warm-gray mb-4 font-medium">
        管理
      </p>
      <h1 className="mb-3">编辑作品</h1>
      <p className="text-charcoal-soft text-sm">请先登录。</p>
    </div>
  );
}
```

---

## API 接口一览

### 管理端（需 Bearer Token）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/projects?page=0&size=20` | 分页列表（priority DESC, createdAt DESC） |
| GET | `/api/admin/projects/{id}` | 按 ID 获取 |
| POST | `/api/admin/projects` | 创建（name/slug 必填校验） |
| PUT | `/api/admin/projects/{id}` | 更新 |
| DELETE | `/api/admin/projects/{id}` | 删除 |

### 公开接口（未受影响）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/projects` | 作品列表 |
| GET | `/api/projects/{slug}` | 作品详情 |

---

## 验证结果

### 后端

```bash
cd backend
mvn test
```

```
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 前端

```bash
cd frontend
npm run lint    # 仅剩预存的 react-hooks/set-state-in-effect 模式（与 posts/page.tsx 一致）
npm run build   # BUILD SUCCESS，全部路由生成
```

路由输出：

```
├ ○ /admin
├ ○ /admin/posts
├ ƒ /admin/posts/[id]
├ ○ /admin/posts/new
├ ○ /admin/projects        ← 新增
├ ƒ /admin/projects/[id]   ← 新增
├ ○ /admin/projects/new    ← 新增
├ ƒ /blog
├ ƒ /blog/[slug]
├ ○ /contact
├ ○ /works
└ ƒ /works/[slug]
```

### 手动验证路径

| 路径 | 预期 |
|------|------|
| `/admin` | 显示「作品管理」+「新建作品」入口 |
| `/admin/projects` | 未登录→登录提示；登录后→作品列表/空状态 |
| `/admin/projects/new` | AuthGuard 保护→ProjectEditor 新建模式 |
| `/admin/projects/[id]` | AuthGuard 保护→加载已有数据→编辑模式 |
| `/admin/posts` | 不受影响 |
| `/admin/posts/new` | 不受影响 |
| `/admin/posts/[id]` | 不受影响 |
