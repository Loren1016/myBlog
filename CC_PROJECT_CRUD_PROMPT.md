# CC Prompt: 作品管理 CRUD 后端接口

请只实现「作品 Project 的后端管理端 CRUD 接口」，不要做前端页面，不要改作品公开页面。

## 项目背景

- 后端是 Spring Boot 3.2 + Java 17 + Maven + JPA + MySQL。
- 现有博客管理端 CRUD 已完成，可参考：
  - `backend/src/main/java/com/myblog/controller/AdminBlogController.java`
  - `backend/src/main/java/com/myblog/service/BlogService.java`
- Project 相关骨架已存在：
  - entity: `backend/src/main/java/com/myblog/entity/Project.java`
  - repository: `backend/src/main/java/com/myblog/repository/ProjectRepository.java`
  - service: `backend/src/main/java/com/myblog/service/ProjectService.java`
  - public controller: `backend/src/main/java/com/myblog/controller/ProjectController.java`
- `SecurityConfig` 已要求 `/api/admin/**` 需要 Bearer Token 认证，所以新增管理接口放在 `/api/admin/projects` 即可自动受保护。
- 统一响应使用 `ApiResponse<T>`。

## 目标

实现作品管理端 CRUD API，接口风格尽量与 `AdminBlogController` 保持一致。

## 需要实现的接口

### 1. 作品列表

`GET /api/admin/projects`

- 返回所有作品。
- 支持 `page` 和 `size` 参数，默认 `page=0`、`size=20`。
- 返回结构尽量和 `/api/admin/posts` 一致：

```json
{
  "content": [],
  "totalElements": 0,
  "totalPages": 0,
  "page": 0
}
```

- 作品列表建议按 `priority` 降序，再按 `completedAt` 降序或 `createdAt` 降序排序。

### 2. 获取单个作品

`GET /api/admin/projects/{id}`

- 根据 `id` 获取单个作品。
- 找不到时抛出：

```java
new ResourceNotFoundException("Project", "id", id)
```

### 3. 创建作品

`POST /api/admin/projects`

- 创建作品。
- `RequestBody` 可以使用 `Project` 实体，或使用现有项目风格中的 DTO。
- 优先保持与 `AdminBlogController` 一致。
- 返回创建后的 `Project`。

### 4. 更新作品

`PUT /api/admin/projects/{id}`

- 更新作品。
- 需要更新字段：
  - `name`
  - `slug`
  - `tagline`
  - `description`
  - `coverImage`
  - `screenshots`
  - `techStack`
  - `demoUrl`
  - `sourceUrl`
  - `priority`
  - `role`
  - `completedAt`
- 返回更新后的 `Project`。

### 5. 删除作品

`DELETE /api/admin/projects/{id}`

- 删除作品。
- 返回：

```java
ApiResponse.success(null)
```

## 实现要求

- 新增 `AdminProjectController.java`：
  - `backend/src/main/java/com/myblog/controller/AdminProjectController.java`
- 如果 `ProjectService` 缺少 `findById` 或分页查询方法，请补上。
- 如果 `ProjectRepository` 缺少分页排序所需方法，可以直接使用 `JpaRepository` 的 `findAll(Pageable pageable)`。
- 不要破坏现有公开接口：
  - `GET /api/projects`
  - `GET /api/projects/{slug}`
- 不要修改认证逻辑，保持 `/api/admin/**` 由现有 `SecurityConfig` 保护。
- 保持代码风格与现有 `AdminBlogController`、`ProjectService` 一致。
- 只做后端管理端接口，不做前端页面和前端 API 封装。

## 变更代码展示要求

实现完成后，请在项目根目录新增或更新一个 Markdown 文件：

```text
PROJECT_CRUD_CHANGES.md
```

这个文件必须包含：

1. 本次新增/修改文件列表。
2. 每个新增/修改代码文件的「修改后完整代码」。
3. 每个文件用清晰的小标题标明路径。
4. 每段代码使用 Markdown fenced code block，并写明语言，例如：

```markdown
## backend/src/main/java/com/myblog/controller/AdminProjectController.java

```java
// 完整代码
```
```

5. 不要只写摘要，不要只写 diff；需要贴出所有被修改或新增代码文件的完整最终内容。
6. 不需要贴出 `target/`、日志、IDE 配置、依赖缓存等生成文件。
7. 在文件末尾追加验证方式，包括运行过的命令、结果摘要，以及失败原因（如果有）。

## 验证要求

完成后请运行后端测试，或至少运行：

```bash
cd backend
mvn test
```

如果测试环境因为数据库不可用或本地配置问题失败，请说明失败原因。

## 验收标准

- 未登录访问 `GET /api/admin/projects` 返回 401 JSON。
- 登录后携带 Bearer Token 可以：
  - 创建作品
  - 查询作品列表
  - 根据 id 查询作品
  - 更新作品
  - 删除作品
- 公开接口 `GET /api/projects` 仍然可用。
- 根目录存在 `PROJECT_CRUD_CHANGES.md`，并且里面展示了本次所有新增/修改代码文件的完整最终代码。
