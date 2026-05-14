
<<<<<<< HEAD
此文件为 Claude Code (claude.ai/code) 提供当前仓库的开发指引。

## 项目概述

个人博客 & 作品展示网站 — 支持 Markdown 编写技术博客、展示个人开发项目。单用户系统（博主本人使用），中文内容，英文代码标识。当前处于 **Phase 1** 阶段（项目脚手架 + 前端视觉已完成，后端骨架完成，JWT 认证流程已实现）。

## 架构

```
frontend/   — Next.js 16 (App Router) + Tailwind CSS v4 + TypeScript
backend/    — Java 17 + Spring Boot 3.2 + Maven + JPA/Hibernate + MySQL 8.0
```

博客和作品相关页面（列表/详情）均已对接真实后端 API，通过 `src/lib/api.ts` 调用 `http://localhost:8080/api`。管理后台的博客和作品 CRUD 均已完整接入后端。

**数据流向：** Next.js 页面 → `src/lib/api.ts` → Spring Boot REST API → JPA Repository → MySQL

**Markdown 渲染：** 博客正文使用 `react-markdown` + `remark-gfm`，具体见 `MarkdownContent.tsx` 组件。

**CORS：** 后端 `WebConfig.java` 允许 `localhost:3000`。

**安全规则：** `SecurityConfig.java` 使用无状态 JWT 认证。公开端点：`/api/blogs/**`、`/api/posts/**`（两者等效）、`/api/projects/**`、`POST /api/auth/admin/login`、`POST /api/auth/admin/refresh` 无需认证。`/api/admin/**` 和 `/api/auth/**` 其余端点需 Bearer Token。401/403 返回 JSON 格式错误信息。

**默认管理员：** `admin@myblog.com` / `admin123`（由 `DataInitializer.java` 在首次启动时自动创建）。

## 常用命令

### 前端（在 `frontend/` 目录执行）

```bash
npm run dev       # 启动 Next.js 开发服务器 localhost:3000
npm run build     # 生产构建
npm run start     # 启动生产服务器
npm run lint      # 运行 ESLint
```

### 后端（在 `backend/` 目录执行）

```bash
mvn spring-boot:run                              # 启动后端 localhost:8080
mvn test                                         # 运行全部测试
mvn test -Dtest=ClassName                        # 运行单个测试类
mvn clean package -DskipTests                    # 打包 JAR
```

### 数据库

MySQL 8.0 `127.0.0.1:3306`，数据库 `myblog`，用户名 `root`，密码 `1234`。四张表（`blog`、`project`、`user`、`media`）由 JPA `ddl-auto=update` 自动创建。

**后端配置：** `application.yml` 中 JWT 密钥默认 `change-me-to-a-strong-random-secret-key`，Token 有效期 7 天。文件上传限制 10MB（单个文件）/ 20MB（请求），上传目录 `./uploads`。

**前端环境变量：** `NEXT_PUBLIC_API_URL` 控制 API 基础地址，默认 `http://localhost:8080/api`。在 `frontend/.env.local` 中覆盖。

### 验证是否正常启动

```bash
curl -s http://localhost:3000          # 前端
curl -s http://localhost:8080/api/blogs  # 后端 API（预期返回 {"code":200,"data":[]}）
```

## 重要：Next.js 16 破坏性变更

此项目使用 Next.js **16.2.6**，与旧版本存在破坏性变更。在编写任何 Next.js 代码之前，先查阅 `frontend/node_modules/next/dist/docs/` 中的最新 API 文档。`frontend/AGENTS.md` 和 `frontend/CLAUDE.md` 均对此有提醒。

## 前端设计系统

视觉语言遵循 **Polene Paris** 美学 — 暖中性低饱和色板、编辑式排版、克制动效、充足留白。无厚重阴影、无高亮强调色、无"创业 SaaS"模板感。

- **背景色：** `cream` (#faf7f2)，卡片：`cream-deep` (#f3efe8)
- **文字：** `charcoal` (#3a3633)，正文：`charcoal-soft` (#5c5754)
- **强调色：** `sage-deep` (#8a9a83) 用于链接和重点
- **字体：** Playfair Display（标题）、Inter（正文）、JetBrains Mono（代码）
- **动效：** `fade-up`、`fade-in`、`reveal-line`、`soft-scale` — 全部定义在 `globals.css`，通过 `.reveal`、`.reveal-child`、`FadeIn`、`FadeInStagger` 组件触发

CSS 设计 Token（颜色、字体、动效、噪点纹理覆盖层）集中在 [frontend/src/app/globals.css](frontend/src/app/globals.css)。

## 关键文件与约定

### 用户可编辑的内容配置

[frontend/src/lib/content.ts](frontend/src/lib/content.ts) 是用户唯一需要编辑的配置文件，用于：
- 将项目 slug 映射到封面图和截图组（`projectImages`）
- 将博客 slug 映射到封面图（`postImages`）
- 为每个作品设置演示/源码链接（`projectLinks`）
- 设置关于页照片和默认回退占位图

图片放在 `frontend/public/images/`，通过 `/images/文件名.jpg` 引用。`ImageOrPlaceholder` 组件先尝试加载图片，失败时自动回退为渐变色占位。

### 路由命名约定

文件夹/文件名和 URL slug 使用**英文**，前端 UI 显示中文标签（完整对照见 [ROUTE_MAP.md](ROUTE_MAP.md)）。示例：

| URL | 界面显示 |
|-----|---------|
| `/works` | 作品 |
| `/blog` | 博客 |
| `/about` | 关于 |
| `/contact` | 联系 |

### 组件模式

- **默认使用服务端组件** — 大部分页面为含静态数据的服务端组件
- **客户端组件** 仅在需要交互的地方使用：`Header`（usePathname）、`FadeIn`/`FadeInStagger`（IntersectionObserver）、`ImageOrPlaceholder`（onError 状态）、`ImageReveal`（clip-path 动效）
- **`use client`** 指令显式标记客户端组件
- 可复用 UI 原语在 `src/components/ui/`，页面布局壳在 `src/components/layout/`
- 管理员组件在 `src/components/admin/`（`AuthGuard`、`LoginForm`）

### API 路径注意

博客公开接口有两条等效路径（`BlogController.java` 同时映射两者）：

| 路径 | 说明 |
|------|------|
| `GET /api/blogs` 或 `GET /api/posts` | 博客列表（分页） |
| `GET /api/blogs/{slug}` 或 `GET /api/posts/{slug}` | 博客详情 |

前端统一使用 `/posts` 路径。管理端接口为 `/api/admin/posts`（仅此一条路径）。

### 动态渲染

需要每次请求从 API 拉取数据的页面使用 `export const dynamic = "force-dynamic"` 禁用静态生成（博客列表页、博客详情页）。未来接入 CMS/数据库后所有公开页面都应使用此模式。

### 前端认证流程

[frontend/src/lib/auth.ts](frontend/src/lib/auth.ts) 封装了完整的 Token 生命周期管理（access/refresh token 双令牌），数据流：

```
LoginForm → auth.login() → api.adminLogin() → 后端 /api/auth/admin/login
         ↓
    localStorage 存储 access/refresh token + user 对象
         ↓
AuthGuard → validateSession() → api.adminGetMe() → 后端 /api/auth/admin/me
         ↓
    access token 过期时自动调用 refreshAccessToken()
         ↓
    logout() → api.adminLogout() + 清除本地存储
```

Token 持久化在 localStorage，key 分别为 `admin_access_token`、`admin_refresh_token`、`admin_user`。

### 后端分层结构

```
Controller → Service → Repository → Entity
    ↓           ↓
   DTO        DTO
```

- **Entity** 类通过 `@PrePersist` 自动生成 UUID 并设置时间戳，`@PreUpdate` 自动更新 `updatedAt`
- **DTO** 类映射到 `ApiResponse<T>` 统一封装：`{code, message, data}`
- **JSON 字段**（tags、techStack、skills、screenshots）使用 `@Column(columnDefinition = "JSON")` + JPA 转换器实现与 `List<String>` 的双向转换
- **异常处理：** `GlobalExceptionHandler`（`@RestControllerAdvice`）捕获 `ResourceNotFoundException`（返回 404）和通用异常（返回 500）
- **Lombok：** Entity/DTO 使用 `@Getter`/`@Setter`/`@NoArgsConstructor` 等注解减少样板代码

### 管理后台路由

| URL | 功能 |
|-----|------|
| `/admin` | 仪表盘 + 登录入口 |
| `/admin/posts` | 文章列表（分页、删除） |
| `/admin/posts/new` | 新建文章 |
| `/admin/posts/[id]` | 编辑文章 |
| `/admin/projects` | 作品列表（分页、删除） |
| `/admin/projects/new` | 新建作品 |
| `/admin/projects/[id]` | 编辑作品 |

新建和编辑共用对应 `PostEditor.tsx` / `ProjectEditor.tsx` 组件（通过可选 `id` prop 区分）。

## 当前进度（Phase 1）

**已完成：**
- 8 个前端页面（首页、作品列表/详情、博客列表/详情、关于、联系、管理后台），视觉效果完整
- 全部 UI 组件（Header、Footer、FadeIn、ImageOrPlaceholder、SectionHeading、ImageReveal、HeroBackground 等）
- 后端 Blog、Project、Auth、Media 的完整 CRUD
- JWT 认证流程（access/refresh 双令牌、token version 吊销、前端 AuthGuard/LoginForm）
- 管理员登录页面可用（`admin@myblog.com` / `admin123`）
- 博客管理 CRUD 已完整实现：`AdminBlogController` + 前端 `PostEditor` + 文章列表/新建/编辑页面
- 作品管理 CRUD 已完整实现：`AdminProjectController` + 前端 `ProjectEditor` + 作品列表/新建/编辑页面
- 博客和作品公开页面均已对接真实后端 API，支持 Markdown 渲染
- 4 张 MySQL 表已创建并验证
- 前后端 CORS 已配置，SecurityConfig 强制执行 `/api/admin/**` 认证
- 图片上传后端接口已实现（`POST /api/admin/media/upload`，含文件校验、安全扩展名、MediaUploadResponse DTO、孤儿文件清理）

**已完成：**
- 图片上传前端集成 — `ImageUploader` 组件已接入 PostEditor 和 ProjectEditor

**待实现：**
- 管理后台仪表盘接入真实统计数据
- 部署上线
