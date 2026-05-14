# 项目开发进度

> 更新日期: 2026-05-14（仪表盘真实统计接入；博客 + 作品管理 CRUD 完成；全部公开页面对接真实 API；图片上传后端实现）

---

## 当前阶段: Phase 1 — 项目脚手架 + 前端视觉

---

## 一、前端 (Next.js 16 + Tailwind CSS v4 + TypeScript)

### 1.1 设计系统

文件: [src/app/globals.css](frontend/src/app/globals.css)

**色彩方案** — 暖中性低饱和色板（Polene 风格）

| Token | 色值 | 用途 |
|-------|------|------|
| `cream` | `#faf7f2` | 全局背景色 |
| `cream-deep` | `#f3efe8` | 卡片/区块表面色 |
| `sand` | `#e8dfd5` | 渐变点缀 |
| `warm-gray` | `#b8a99a` | 次要文字、边框 |
| `warm-gray-light` | `#d4cbc2` | hover 状态边框 |
| `charcoal` | `#3a3633` | 主文字色 |
| `charcoal-soft` | `#5c5754` | 正文段落色 |
| `sage` | `#c5cfc0` | 淡绿点缀 |
| `sage-deep` | `#8a9a83` | 强调色 (链接、选中) |
| `sage-dark` | `#6b7d64` | 深绿 hover |
| `stone` | `#d9d2c7` | 渐变点缀 |

**字体系统**

| 用途 | 字体 | 加载方式 |
|------|------|----------|
| 标题 (h1-h4) | Playfair Display (serif) | Google Fonts |
| 正文/界面 | Inter (sans-serif, weight 300/400/500) | Google Fonts |
| 代码 | JetBrains Mono (monospace) | Google Fonts |

**排版层级**
- h1: `clamp(3rem, 6vw, 5.5rem)` / line-height 1.08 / weight 400
- h2: `clamp(2.25rem, 4.5vw, 3.5rem)` / line-height 1.18
- h3: `clamp(1.35rem, 2.5vw, 1.75rem)` / line-height 1.35
- body: 16px / weight 300 / line-height 1.8

**视觉效果**
- 全局噪点纹理: CSS SVG `feTurbulence` filter，opacity 0.025，固定覆盖层
- 选中文字高亮: sage 色 35% 透明度背景
- 水平分割线: 1px solid warm-gray 25%

**动画关键帧** (定义在 globals.css)

| 动画名 | 效果 |
|--------|------|
| `fade-up` | opacity 0→1 + translateY(28px→0) |
| `fade-in` | opacity 0→1 |
| `reveal-line` | opacity 0→1 + translateY(12px→0) |
| `soft-scale` | opacity 0→1 + scale(0.97→1) |

**滚动揭示类**
- `.reveal` — 单个元素，进入视口时触发 fade-up
- `.reveal-child` — 容器类，子元素依次错开淡入 (nth-child 1-5，间隔 100ms)

**链接动效**
- `.link-underline` — hover 时底部 1px 线条从右向左滑动展开 (scaleX + transform-origin 动画)

**图片揭示**
- `.img-reveal` — clip-path inset 从底部向上展开 (1s cubic-bezier)
- hover 时 scale(1.03) 轻微放大

---

### 1.2 UI 组件

#### ImageOrPlaceholder
文件: [components/ui/ImageOrPlaceholder.tsx](frontend/src/components/ui/ImageOrPlaceholder.tsx)

- Client component，支持真实图片 + 渐变色回退
- 接收 `src`、`alt`、`aspectRatio`、`gradient` 参数
- 图片加载失败时自动触发 `onError`，隐藏 `<img>` 显示渐变占位
- 含装饰性模糊圆形背景层
- 图片 hover 时 scale(1.03) 2 秒过渡

#### FadeIn
文件: [components/ui/FadeIn.tsx](frontend/src/components/ui/FadeIn.tsx)

- Client component，Intersection Observer 触发淡入
- Props: `delay` (ms)、`threshold` (0-1)、`as` (HTML 标签)
- 进入视口后 `setVisible(true)`，添加 `.visible` 类触发 CSS transition
- 只触发一次（进入后 unobserve）

#### FadeInStagger
文件: [components/ui/FadeIn.tsx](frontend/src/components/ui/FadeIn.tsx)

- 同上文件，独立导出
- 子元素通过 `.reveal-child.visible > *:nth-child(n)` 错开延迟

#### SectionHeading
文件: [components/ui/SectionHeading.tsx](frontend/src/components/ui/SectionHeading.tsx)

- 统一样式的章节标题组件
- Props: `label` (小号大写标签)、`title` (h2 标题)
- 结构: 标签 → h2 → hr

#### Header
文件: [components/layout/Header.tsx](frontend/src/components/layout/Header.tsx)

- Client component（使用 `usePathname` 判断当前路由）
- 左侧: "Portfolio" 衬线斜体 Logo，链接到首页
- 右侧: 导航栏四个链接 — 作品(/works)、博客(/blog)、关于(/about)、联系(/contact)
- 当前路由高亮: `text-charcoal`，其余 `text-warm-gray`
- 导航标签中文显示: "作品"、"博客"、"关于"、"联系"
- hover 时 `.link-underline` 动画

#### Footer
文件: [components/layout/Footer.tsx](frontend/src/components/layout/Footer.tsx)

- 顶部分割线 + 版权信息 "© 2026 保留所有权利"
- 社交链接: GitHub、Email、Twitter
- 全部使用 `.link-underline` 动效

#### Container
文件: [components/layout/Container.tsx](frontend/src/components/layout/Container.tsx)

- `max-w-7xl mx-auto px-6` 宽度约束容器
- Props: `className` 扩展

#### ImageReveal (备选)
文件: [components/ui/ImageReveal.tsx](frontend/src/components/ui/ImageReveal.tsx)

- 独立图片揭示组件（使用 clip-path 动画）
- Props: `src`、`alt`、`aspectRatio`、`threshold`

#### AuthGuard
文件: [components/admin/AuthGuard.tsx](frontend/src/components/admin/AuthGuard.tsx)

- Client component，校验登录态
- 启动时读取 localStorage 中的 token，调用 `GET /api/auth/admin/me` 验证
- 验证失败时自动尝试 refresh token 刷新
- 三种状态：loading / authenticated (渲染 children) / unauthenticated (渲染 fallback)

#### LoginForm
文件: [components/admin/LoginForm.tsx](frontend/src/components/admin/LoginForm.tsx)

- Client component，管理员登录表单
- 邮箱 + 密码输入，底线输入框风格（遵循现有设计系统）
- 表单校验、加载态、错误提示
- 登录成功后存储 token 到 localStorage 并 reload 页面

#### auth.ts
文件: [src/lib/auth.ts](frontend/src/lib/auth.ts)

- Token 持久化（localStorage: `admin_access_token`, `admin_refresh_token`, `admin_user`）
- 对外 API: `login()`, `logout()`, `refreshAccessToken()`, `validateSession()`, `getCachedUser()`, `getAccessToken()`, `isAuthenticated()`

---

### 1.3 页面路由详情

#### `/` — 首页
文件: [src/app/page.tsx](frontend/src/app/page.tsx)

数据来源: 精选作品 — `getProjects()` → `GET /api/projects` 真实 API，取前 2 篇（按 priority 排序）+ `projectImages` 作为封面图回退；最新文章 — `getPublishedBlogs()` → `GET /api/posts` 真实 API，取前 3 篇 · `export const dynamic = "force-dynamic"` · 两个请求通过 `Promise.all` 并行加载

**Hero 区**
- 小标签 "开发者 & 写作者" (0.1s 淡入)
- 主标题 "用心构建软件，用文字记录关于技艺的思考。" (0.3s 淡入)
- 副标题 (0.6s 淡入) — 三段文本依次动画
- 最小高度 85vh，垂直居中

**精选作品区**
- SectionHeading label="精选作品" title="项目"
- 2 列网格，第一个项目跨 2 列
- 卡片使用 `ImageOrPlaceholder`（图片 → 渐变色回退）
- hover 时: 暗色叠层淡入 + 底部信息上移
- 底部信息: 分类标签、项目名 (h3)、简介
- 数据: `getProjects()` API 返回的前 2 个作品（按 priority DESC 排序）；无作品时显示空状态
- 底部 "查看所有作品 →" 链接到 /works

**最新文章区**
- SectionHeading label="最新文章" title="博客"
- 从 `/api/posts` 动态拉取，取前 3 篇 published 文章
- 日期 + 标题 + 分类的编辑式列表；hover 行背景变 `cream-deep/50`
- API 失败或无文章时显示空状态
- 底部 "阅读所有文章 →" 链接到 /blog

**联系 CTA**
- 衬线斜体 "有想法或合作意向？"
- "取得联系" 底线链接 → /contact

#### `/works` — 作品列表页
文件: [src/app/works/page.tsx](frontend/src/app/works/page.tsx)

数据来源: `getProjects()` → `GET /api/projects` 真实后端 API · `export const dynamic = "force-dynamic"` · 封面图优先取 API 返回的 `coverImage`，回退到 `projectImages` 配置

- SectionHeading label="项目" title="作品"
- 2 列网格，`gap-[2px]`，首项 `lg:col-span-2` (aspect-ratio 16/6)，其余 4/3
- 每个卡片: `ImageOrPlaceholder` 图片区、hover 暗色叠层(z-10)、底部信息(z-20)
- 信息: 年份 (completedAt) + 技术栈标签 (techStack) + 项目名 + 简介
- hover 效果: 叠层淡入 + 信息上移 + 文字变白
- API 错误时显示 "暂时无法加载作品。"；无作品时显示 "还没有作品。"
- 点击跳转 `/works/[slug]` 详情页

#### `/works/[slug]` — 作品详情页 (动态路由)
文件: [src/app/works/[slug]/page.tsx](frontend/src/app/works/[slug]/page.tsx)

数据来源: `getProjectBySlug(slug)` → `GET /api/projects/{slug}` 真实后端 API · `export const dynamic = "force-dynamic"` · slug 不存在时 `notFound()` · 封面图优先取 API 返回的 `coverImage`，回退到 `projectImages`；链接优先取 API 的 `demoUrl`/`sourceUrl`，回退到 `projectLinks` 配置

- 动态路由，`params.slug` 获取项目标识
- slug 不存在时触发 Next.js 404 页面
- 返回链接 "← 所有作品"

**页面结构**
1. Hero 大图 (16/7，ImageOrPlaceholder)
2. Meta 标签行: 年份 (completedAt) + 技术栈标签 (techStack)
3. 项目名称 (h1)
4. 一句话简介 (tagline, xl)
5. 分割线
6. 详情正文区: `description` 通过 `MarkdownContent` 渲染
7. 截图区: `screenshots` 数组以 2 列网格展示（仅当有截图时）
8. 底部链接区:
   - 有 `demoUrl` → 显示 "在线演示 →" (新窗口打开)
   - 有 `sourceUrl` → 显示 "源代码 →" (新窗口打开)
   - 都没有 → 显示 "链接待配置"

#### `/blog` — 博客列表页
文件: [src/app/blog/page.tsx](frontend/src/app/blog/page.tsx)

数据来源: `getPublishedBlogs()` → `GET /api/posts` 真实后端 API · `export const dynamic = "force-dynamic"`

- 小标签 "写作" + h1 "博客"
- 副标题: "关于软件、设计以及用心做东西的思考与记录。"
- 标签筛选按钮栏: 方法论 / 工程 / 设计 / 工具 / 反思 (目前仅展示，筛选逻辑未实现)
- 文章列表从后端 API 动态拉取，显示: 日期 + 分类标签 + 标题(h2) + 摘要
- hover 行背景 `cream-deep/30`
- 无文章时显示 "还没有文章。" 空状态
- API 错误时显示 "暂时无法加载文章。" 错误提示
- 点击跳转 `/blog/[slug]` 详情页

#### `/blog/[slug]` — 文章详情页 (动态路由)
文件: [src/app/blog/[slug]/page.tsx](frontend/src/app/blog/[slug]/page.tsx) · [MarkdownContent.tsx](frontend/src/app/blog/[slug]/MarkdownContent.tsx)

数据来源: `getPublishedBlogBySlug(slug)` → `GET /api/posts/{slug}` 真实后端 API · `export const dynamic = "force-dynamic"` · slug 不存在时 `notFound()`

- 返回链接 "← 博客"
- Meta: 日期 + 分类标签
- 标题 h1
- 封面图 (ImageOrPlaceholder, 3/2，仅当有 coverImage 时显示)
- 分割线

**正文排版**
- 通过 `MarkdownContent` 组件使用 `react-markdown` + `remark-gfm` 渲染 Markdown 正文
- 排版样式在 `.prose-custom` 中定义
- 底部 "← 返回博客" 链接

#### `/about` — 关于页
文件: [src/app/about/page.tsx](frontend/src/app/about/page.tsx)

数据来源: 文件内 `skills` 和 `experience` 数组

- 小标签 "关于" + h1 "你好。" + 分割线

**布局** — 双栏 (lg:grid-cols-[1fr_2fr])
1. 我是谁: 左侧标签 + 右侧三段自我介绍
2. 经历: 左侧标签 + 右侧时间线（时段 / 职位 / 公司）
3. 技能 & 工具: 左侧标签 + 右侧 10 个技能标签（带边框，hover 变色）

#### `/contact` — 联系页
文件: [src/app/contact/page.tsx](frontend/src/app/contact/page.tsx)

数据来源: 文件内 `links` 数组

- 小标签 "联系" + h1 "取得联系。" + 副标题

**布局** — 双栏 (lg:grid-cols-[1fr_1fr])
1. 发送消息表单:
   - 姓名 (底线输入框)
   - 邮箱 (底线输入框)
   - 留言 (多行底线输入框)
   - "发送消息" 底线按钮
   - 注意: 表单为纯展示，`onSubmit` 未绑定（服务端组件限制）
2. 其他渠道:
   - GitHub / Email / Twitter / LinkedIn (衬线斜体大号链接)
   - 分割线 + "通常在 24–48 小时内回复。"

#### `/admin` — 管理后台仪表盘
文件: [src/app/admin/page.tsx](frontend/src/app/admin/page.tsx)

- AuthGuard 包裹：未登录时显示 LoginForm，已登录显示 Dashboard
- **登录态**: 通过 AuthGuard 校验 — 读取 localStorage token → `GET /api/auth/admin/me` → 失败则尝试 refresh → 仍失败则显示登录表单
- **登录表单 (LoginForm)**: 小标签 "管理" + h1 "登录" + 邮箱/密码底线输入框 + 登录按钮
- **仪表盘 (Dashboard)**:
  - 右上角显示当前管理员姓名、邮箱、退出登录按钮
  - 4 个统计卡片 (2x2 网格): 已发布文章 / 草稿 / 作品数量 / 媒体文件 — 数值通过 `GET /api/admin/dashboard/stats` 实时查询
  - 最近更新的文章列表 (最近 5 条，标题 + slug + 日期 + 状态标签，点击跳转编辑页)
  - 最近更新的作品列表 (最近 5 条，名称 + slug + 日期，点击跳转编辑页)
  - 4 个快捷入口卡片 (2x2 网格):
    - 新建文章 → `/admin/posts/new`
    - 文章管理 → `/admin/posts`
    - 作品管理 → `/admin/projects`
    - 新建作品 → `/admin/projects/new`
- **退出登录**: 调用 `POST /api/auth/admin/logout` + 清除 localStorage → 返回登录表单

#### `/admin/posts` — 文章管理列表
文件: [src/app/admin/posts/page.tsx](frontend/src/app/admin/posts/page.tsx)

- AuthGuard 保护，通过 `adminListPosts()` 调用 `GET /api/admin/posts` 获取全部文章
- 显示文章数量 + 当前管理员名
- 顶部操作栏: "新建文章" 链接 + "仪表盘" 返回链接
- 文章列表: 标题 (可点击编辑)、状态标签 (已发布/草稿)、slug、发布日期
- 每篇文章有 "编辑" 链接和 "删除" 按钮 (含确认/取消两步操作)
- 空列表时显示 "还没有文章。" + 新建入口
- 加载中和错误状态均有处理

#### `/admin/posts/new` — 新建文章
文件: [src/app/admin/posts/new/page.tsx](frontend/src/app/admin/posts/new/page.tsx)

- AuthGuard 保护，渲染 `<PostEditor />`（无 `id` prop）

#### `/admin/posts/[id]` — 编辑文章
文件: [src/app/admin/posts/[id]/page.tsx](frontend/src/app/admin/posts/[id]/page.tsx)

- 通过 `useParams()` 获取文章 ID，渲染 `<PostEditor id={id} />`

#### PostEditor — 文章编辑器（共用组件）
文件: [src/app/admin/posts/PostEditor.tsx](frontend/src/app/admin/posts/PostEditor.tsx)

- 通过可选 `id` prop 区分新建/编辑模式
- 编辑模式下启动时调用 `adminGetPost()` 加载已有数据
- 表单字段: 标题、Slug、分类、摘要、标签（逗号分隔）、封面图 URL、正文（Markdown 文本区）
- 两个提交按钮: "保存草稿"（status=draft）+ "发布"（status=published）
- 新建成功后自动跳转到编辑页 (`router.replace`)
- 保存/发布后显示 "已保存。" 成功提示（2 秒自动消失）

#### `/admin/projects` — 作品管理列表
文件: [src/app/admin/projects/page.tsx](frontend/src/app/admin/projects/page.tsx)

- AuthGuard 保护，通过 `adminListProjects()` 调用 `GET /api/admin/projects` 获取全部作品
- 显示作品数量 + 当前管理员名
- 顶部操作栏: "新建作品" 链接 + "仪表盘" 返回链接
- 作品列表: 名称 (可点击编辑)、置顶标签 (priority > 0 时)、slug、tagline、技术栈预览
- 每件作品有 "编辑" 链接和 "删除" 按钮 (含确认/取消两步操作)
- 空列表时显示 "还没有作品。" + 新建入口
- 加载中和错误状态均有处理

#### `/admin/projects/new` — 新建作品
文件: [src/app/admin/projects/new/page.tsx](frontend/src/app/admin/projects/new/page.tsx)

- AuthGuard 保护，渲染 `<ProjectEditor />`（无 `id` prop）

#### `/admin/projects/[id]` — 编辑作品
文件: [src/app/admin/projects/[id]/page.tsx](frontend/src/app/admin/projects/[id]/page.tsx)

- 通过 `useParams()` 获取作品 ID，渲染 `<ProjectEditor id={id} />`

#### ProjectEditor — 作品编辑器（共用组件）
文件: [src/app/admin/projects/ProjectEditor.tsx](frontend/src/app/admin/projects/ProjectEditor.tsx)

- 通过可选 `id` prop 区分新建/编辑模式
- 编辑模式下启动时调用 `adminGetProject()` 加载已有数据
- 表单字段: 作品名称 (\*)、Slug (\*)、优先级（数字）、一句话简介、封面图 URL、详细介绍（Markdown 文本区）、技术栈（逗号分隔 → JSON 字符串）、截图 URL（逗号分隔 → JSON 字符串）、演示链接、源码链接、我的角色、完成日期
- `screenshots` / `techStack` 字段转换: 后端存储为 JSON 字符串 `["a","b"]`，编辑器用逗号分隔文本输入/展示，通过 `jsonArrayStringToCommaText` / `commaTextToJsonArrayString` 双向转换
- 前端校验: name/slug 非空、priority 为数字
- 后端校验: `@NotBlank` on name/slug、`@Size(max=200)` on tagline（返回 400 + 中文错误信息）
- 新建成功后自动跳转到编辑页 (`router.replace`)
- 保存后显示 "已保存。" 成功提示（2 秒自动消失）

---

### 1.4 配置与工具模块

#### 内容映射配置 — content.ts
文件: [src/lib/content.ts](frontend/src/lib/content.ts)

这是**用户唯一需要编辑的配置文件**，集中管理:

| 配置项 | 说明 |
|--------|------|
| `projectImages` | 5 个作品的 hero 图路径和 gallery 图片组 |
| `postImages` | 5 篇文章的封面图路径 |
| `projectLinks` | 每个作品的 demo/source (GitHub) 链接 |
| `aboutImage` | 关于页个人照片 |
| `placeholderImage` | 默认回退占位图 |

**工作原理**
- 图片文件放到 `public/images/` 目录
- 在 content.ts 里写 `/images/你的文件名.jpg`
- `ImageOrPlaceholder` 组件优先加载图片，失败自动回退渐变色
- 不需要重启前端，Next.js HMR 热更新

#### API 客户端 — api.ts
文件: [src/lib/api.ts](frontend/src/lib/api.ts)

- `API_BASE`: `http://localhost:8080/api` (可通过环境变量 `NEXT_PUBLIC_API_URL` 覆盖)
- 统一返回类型 `ApiResponse<T>`: `{code, message, data}`
- 统一错误处理: 非 2xx 响应自动 throw Error

**公开接口:**
| 函数 | HTTP | 说明 |
|------|------|------|
| `getPublishedBlogs(page?)` | `GET /api/posts` | 博客列表（分页） |
| `getPublishedBlogBySlug(slug)` | `GET /api/posts/{slug}` | 博客详情 |
| `getProjects(params?)` | `GET /api/projects` | 作品列表 |
| `getProjectBySlug(slug)` | `GET /api/projects/{slug}` | 作品详情 |

**认证接口:**
| 函数 | HTTP | 说明 |
|------|------|------|
| `adminLogin(email, password)` | `POST /api/auth/admin/login` | 管理员登录 |
| `adminRefresh(refreshToken)` | `POST /api/auth/admin/refresh` | 刷新 token |
| `adminLogout(token)` | `POST /api/auth/admin/logout` | 退出登录 |
| `adminGetMe(token)` | `GET /api/auth/admin/me` | 获取当前用户 |

**管理端博客 CRUD (需 Bearer Token):**
| 函数 | HTTP | 说明 |
|------|------|------|
| `adminListPosts(token, page?)` | `GET /api/admin/posts` | 文章列表（含草稿） |
| `adminGetPost(token, id)` | `GET /api/admin/posts/{id}` | 获取单篇 |
| `adminCreatePost(token, data)` | `POST /api/admin/posts` | 创建文章 |
| `adminUpdatePost(token, id, data)` | `PUT /api/admin/posts/{id}` | 更新文章 |
| `adminDeletePost(token, id)` | `DELETE /api/admin/posts/{id}` | 删除文章 |

**管理端作品 CRUD (需 Bearer Token):**
| 函数 | HTTP | 说明 |
|------|------|------|
| `adminListProjects(token, page?)` | `GET /api/admin/projects` | 作品列表（分页） |
| `adminGetProject(token, id)` | `GET /api/admin/projects/{id}` | 获取单个作品 |
| `adminCreateProject(token, data)` | `POST /api/admin/projects` | 创建作品 |
| `adminUpdateProject(token, id, data)` | `PUT /api/admin/projects/{id}` | 更新作品 |
| `adminDeleteProject(token, id)` | `DELETE /api/admin/projects/{id}` | 删除作品 |

**类型:**
- `PublicProject` — 公开作品类型，`screenshots`/`techStack` 为 `string[]`（API 层自动从 JSON 字符串解析）
- `AdminProject` — 管理端原始类型，`screenshots`/`techStack` 为 `string | null`（匹配后端 JSON 字符串）
- `ProjectInput` — 创建/更新作品请求体
- `PaginatedProjects` — 分页响应 `{content, totalElements, totalPages, page}`

#### 类型定义
- [types/blog.ts](frontend/src/types/blog.ts): `Blog` 接口 (14 字段)
- [types/project.ts](frontend/src/types/project.ts): `Project` 接口 (16 字段)

#### 工具函数 — utils.ts
文件: [src/lib/utils.ts](frontend/src/lib/utils.ts)

- `cn(...classes)`: 类名字符串合并，过滤 falsy 值
- `formatDate(date)`: 日期中文化格式化

---

### 1.5 项目文件结构总览

```
frontend/
├── public/images/              ← 图片放这里
├── src/
│   ├── app/
│   │   ├── globals.css         ← 设计系统 (色彩/字体/动画/纹理)
│   │   ├── layout.tsx          ← 根布局 (字体加载/Header/Footer)
│   │   ├── page.tsx            ← 首页
│   │   ├── works/
│   │   │   ├── page.tsx        ← 作品列表 (对接真实 API)
│   │   │   └── [slug]/
│   │   │       └── page.tsx    ← 作品详情 (对接真实 API + Markdown)
│   │   ├── blog/
│   │   │   ├── page.tsx        ← 博客列表 (对接真实 API)
│   │   │   └── [slug]/
│   │   │       ├── page.tsx    ← 文章详情 (对接真实 API)
│   │   │       └── MarkdownContent.tsx ← Markdown 渲染组件
│   │   ├── about/
│   │   │   └── page.tsx        ← 关于页
│   │   ├── contact/
│   │   │   └── page.tsx        ← 联系页
│   │   └── admin/
│   │       ├── page.tsx        ← 管理后台仪表盘 (登录/仪表盘)
│   │       ├── posts/
│   │       │   ├── page.tsx    ← 文章管理列表
│   │       │   ├── PostEditor.tsx ← 文章编辑器 (新建/编辑共用)
│   │       │   ├── new/
│   │       │   │   └── page.tsx ← 新建文章
│   │       │   └── [id]/
│   │       │       └── page.tsx ← 编辑文章
│   │       └── projects/
│   │           ├── page.tsx    ← 作品管理列表
│   │           ├── ProjectEditor.tsx ← 作品编辑器 (新建/编辑共用)
│   │           ├── new/
│   │           │   └── page.tsx ← 新建作品
│   │           └── [id]/
│   │               └── page.tsx ← 编辑作品
│   ├── components/
│   │   ├── layout/
│   │   │   ├── Header.tsx      ← 导航栏 (Client)
│   │   │   ├── Footer.tsx      ← 页脚
│   │   │   └── Container.tsx   ← 宽度容器
│   │   ├── admin/
│   │   │   ├── AuthGuard.tsx   ← 鉴权守卫 (Client)
│   │   │   └── LoginForm.tsx   ← 管理员登录表单 (Client)
│   │   └── ui/
│   │       ├── FadeIn.tsx      ← 淡入动画 + 错开容器 (Client)
│   │       ├── ImageOrPlaceholder.tsx ← 图片/占位图 (Client)
│   │       ├── ImageReveal.tsx  ← 图片揭示 (Client)
│   │       ├── SectionHeading.tsx ← 章节标题
│   │       └── HeroBackground.tsx ← 首页 Hero 背景纹理
│   ├── lib/
│   │   ├── api.ts              ← 后端 API 调用 (公开 + 认证 + 管理 CRUD)
│   │   ├── auth.ts             ← 登录态管理 (token 持久化/session)
│   │   ├── content.ts          ← ⭐ 图片+链接配置 (用户编辑)
│   │   └── utils.ts            ← 工具函数
│   └── types/
│       ├── blog.ts             ← Blog 类型
│       └── project.ts          ← Project 类型
```

---

## 二、后端 (Java 17 + Spring Boot 3.2 + Maven + MySQL)

### 2.1 项目配置

文件: [backend/pom.xml](backend/pom.xml)

| 依赖 | 版本 | 用途 |
|------|------|------|
| spring-boot-starter-web | 3.2.5 | REST API |
| spring-boot-starter-data-jpa | 3.2.5 | ORM / Hibernate |
| spring-boot-starter-security | 3.2.5 | 认证授权 |
| spring-boot-starter-validation | 3.2.5 | 参数校验 |
| mysql-connector-j | 8.3.0 | MySQL 驱动 |
| lombok | (optional) | 简化代码 |
| jjwt-api/impl/jackson | 0.12.5 | JWT 令牌 |

文件: [backend/src/main/resources/application.yml](backend/src/main/resources/application.yml)

- 端口: 8080
- 数据库: `jdbc:mysql://127.0.0.1:3306/myblog` (用户名 root / 密码 1234)
- JPA: `ddl-auto=update` (自动建表/更新表结构)
- JWT: `app.jwt-secret` + `app.jwt-expiration-ms` (604800000ms = 7 天)
- 文件上传: max 10MB / request 20MB，上传目录 `./uploads`

### 2.2 分层架构

```
Controller  →  Service  →  Repository  →  Entity
    ↓              ↓
   DTO           DTO
```

**Controller** — 接收 HTTP 请求，参数校验，返回 DTO  
**Service** — 业务逻辑，事务管理，Entity ↔ DTO 转换  
**Repository** — JPA 接口，数据访问层  
**Entity** — JPA 实体类，映射 MySQL 表  
**DTO** — 数据传输对象，隔离 API 契约与数据库模型  
**Config** — CORS、Spring Security 配置  
**Exception** — 全局异常处理 + 自定义异常

### 2.3 Entity 层

#### Blog
文件: [entity/Blog.java](backend/src/main/java/com/myblog/entity/Blog.java)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(36) PK | UUID 自动生成 |
| title | VARCHAR(255) | 标题 |
| slug | VARCHAR(255) UNIQUE | URL 标识 |
| summary | VARCHAR(500) | 摘要 |
| content | LONGTEXT | Markdown 正文 |
| coverImage | VARCHAR(500) | 封面图 URL |
| tags | JSON | 标签数组 |
| category | VARCHAR(100) | 分类 |
| status | VARCHAR(20) | draft / published |
| publishedAt | DATETIME | 发布时间 |
| createdAt | DATETIME | 创建时间 (自动) |
| updatedAt | DATETIME | 更新时间 (自动) |
| viewCount | INT | 阅读量 |

#### Project
文件: [entity/Project.java](backend/src/main/java/com/myblog/entity/Project.java)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(36) PK | UUID 自动生成 |
| name | VARCHAR(255) | 项目名称 (@NotBlank) |
| slug | VARCHAR(255) UNIQUE | URL 标识 (@NotBlank) |
| tagline | VARCHAR(200) | 一句话简介 (@Size max=200) |
| description | LONGTEXT | Markdown 详细介绍 |
| coverImage | VARCHAR(500) | 封面图 URL |
| screenshots | JSON | 截图 URL 数组 |
| techStack | JSON | 技术栈标签数组 |
| demoUrl | VARCHAR(500) | 在线演示地址 |
| sourceUrl | VARCHAR(500) | 源码 (GitHub) 地址 |
| priority | INT | 置顶权重 |
| role | VARCHAR(100) | 我的角色 |
| completedAt | DATETIME | 完成日期 |
| createdAt / updatedAt | DATETIME | 时间戳 (自动) |

#### User
文件: [entity/User.java](backend/src/main/java/com/myblog/entity/User.java)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(36) PK | UUID |
| email | VARCHAR(255) UNIQUE | 登录邮箱 |
| passwordHash | VARCHAR(255) | bcrypt 密码 |
| displayName | VARCHAR(100) | 显示名称 |
| avatar | VARCHAR(500) | 头像 URL |
| bio | TEXT | 个人简介 |
| skills | JSON | 技能列表 |
| socialLinks | JSON | 社交链接 |

#### Media
文件: [entity/Media.java](backend/src/main/java/com/myblog/entity/Media.java)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(36) PK | UUID |
| filename | VARCHAR(255) | 原始文件名 |
| url | VARCHAR(500) | 访问路径 |
| size | BIGINT | 文件大小 |
| mimeType | VARCHAR(100) | MIME 类型 |
| uploadedAt | DATETIME | 上传时间 |

> 所有 Entity 含 `@PrePersist` (自动生成 UUID + 设置时间戳) 和 `@PreUpdate` (更新时自动刷新 updatedAt)

### 2.4 Repository 层

| 接口 | 自定义方法 |
|------|-----------|
| BlogRepository | `findBySlug(String slug)`, `countByStatus(String status)`, `findTop5ByOrderByUpdatedAtDesc()` |
| ProjectRepository | `findBySlug(String slug)`, `findTop5ByOrderByUpdatedAtDesc()` |
| UserRepository | `findByEmail(String email)` |
| MediaRepository | (仅继承 JpaRepository) |

### 2.5 Service 层

| 服务 | 状态 | 方法 |
|------|------|------|
| BlogService | 已实现 | `findPublished()`, `findAllPaginated()`, `findById()`, `findBySlug()`, `findBySlugPublished()`, `create()`, `update()`, `delete()` — 含自动设置 publishedAt 逻辑 |
| ProjectService | 已实现 | `findAll()`, `findBySlug()`, `findById()`, `findAllPaginated()`, `create()`, `update()`, `delete()` — 含分页排序 (priority DESC, createdAt DESC)、创建时服务端字段控制、分页参数限制 (page>=0, 1<=size<=100) |
| AuthService | 已实现 | `login()`, `refresh()`, `logout()`, `getMe()`, `getCurrentUser()` — 完整 JWT 认证 |
| MediaService | 骨架 | `save()` |
| DashboardService | 已实现 | `getStats()` — 仪表盘统计：博客/作品/媒体计数 + 最近条目列表 |

### 2.6 Controller 层

| Controller | 路由前缀 | 已实现方法 |
|-----------|---------|-----------|
| BlogController | `/api/blogs` + `/api/posts` (双路径) | `GET /` (分页列表), `GET /{slug}` (详情) |
| AdminBlogController | `/api/admin/posts` | `GET /` (列表含草稿), `GET /{id}`, `POST /` (创建), `PUT /{id}` (更新), `DELETE /{id}` (删除) |
| ProjectController | `/api/projects` | `GET /` (列表), `GET /{slug}` (详情) |
| AdminProjectController | `/api/admin/projects` | `GET /` (分页列表), `GET /{id}`, `POST /` (创建), `PUT /{id}` (更新), `DELETE /{id}` (删除) |
| AuthController | `/api/auth` | `POST /admin/login`, `POST /admin/refresh`, `POST /admin/logout`, `GET /admin/me` |
| MediaController | `/api/admin/media` | `POST /upload` (骨架，待前端集成) |
| AdminDashboardController | `/api/admin/dashboard` | `GET /stats` (仪表盘统计) |

### 2.7 DTO 层

| DTO | 说明 |
|-----|------|
| ApiResponse\<T\> | 统一响应: `{code: 200, message: "success", data: T}` |
| BlogDto | Blog 字段全集 |
| ProjectDto | Project 字段全集 |
| LoginRequest | email + password，`@Valid` 校验 |
| PostStatsDto | 博客统计 DTO |
| ProjectStatsDto | 作品统计 DTO |
| MediaStatsDto | 媒体统计 DTO |
| RecentPostDto | 最近博客摘要 DTO |
| RecentProjectDto | 最近作品摘要 DTO |
| DashboardStatsResponse | 仪表盘响应 DTO（PostStatsDto + ProjectStatsDto + MediaStatsDto + 最近列表） |

### 2.8 配置 & 异常处理

| 文件 | 说明 |
|------|------|
| WebConfig.java | CORS: 允许 `localhost:3000`，所有 HTTP 方法，credentials |
| SecurityConfig.java | 注册 JwtAuthenticationFilter；放行 `/api/auth/admin/login` 和 `/api/auth/admin/refresh`；`/api/admin/**` 需认证；401/403 返回 JSON |
| JwtProvider.java | JWT 生成 (accessToken + refreshToken)、解析、校验 |
| JwtAuthenticationFilter.java | OncePerRequestFilter，从 Authorization header 提取 Bearer token 注入 SecurityContext |
| DataInitializer.java | 启动时检查并创建默认管理员 (admin@myblog.com / admin123) |
| GlobalExceptionHandler.java | `@RestControllerAdvice`: 404 (ResourceNotFound) / 400 (IllegalArgument) / 500 (通用) |
| ResourceNotFoundException.java | 自定义异常: `"{resource} not found with {field}: '{value}'"` |

---

## 三、数据库 (MySQL 8.0 / myblog)

### 3.1 已创建表

| 表名 | 引擎 | 字符集 | 说明 |
|------|------|--------|------|
| blog | InnoDB | utf8mb4 | 博客文章 |
| project | InnoDB | utf8mb4 | 作品项目 |
| user | InnoDB | utf8mb4 | 管理员用户 |
| media | InnoDB | utf8mb4 | 媒体文件 |

> JPA `ddl-auto=update` 自动创建/同步表结构

### 3.2 API 接口状态

**公开接口 (无需认证):**

| 方法 | 路径 | 状态 | 说明 |
|------|------|------|------|
| GET | `/api/blogs` 或 `/api/posts` | 已实现 | 分页列表，返回 `{content, totalElements, totalPages, page}` |
| GET | `/api/blogs/{slug}` 或 `/api/posts/{slug}` | 已实现 | 博客详情，slug 不存在返回 404 |
| GET | `/api/projects` | 已实现 | 作品列表 |
| GET | `/api/projects/{slug}` | 已实现 | 作品详情，slug 不存在返回 404 |

**认证接口:**

| 方法 | 路径 | 状态 | 说明 |
|------|------|------|------|
| POST | `/api/auth/admin/login` | 已实现 | JWT 登录，返回 accessToken + refreshToken + 用户信息 |
| POST | `/api/auth/admin/refresh` | 已实现 | 刷新 access token |
| POST | `/api/auth/admin/logout` | 已实现 | 退出登录（撤销 refresh token） |
| GET | `/api/auth/admin/me` | 已实现 | 获取当前登录管理员信息 |

**管理接口 (需 Bearer Token):**

| 方法 | 路径 | 状态 | 说明 |
|------|------|------|------|
| GET | `/api/admin/posts` | 已实现 | 文章列表（含草稿），分页 |
| GET | `/api/admin/posts/{id}` | 已实现 | 获取单篇文章 |
| POST | `/api/admin/posts` | 已实现 | 创建文章 |
| PUT | `/api/admin/posts/{id}` | 已实现 | 更新文章 |
| DELETE | `/api/admin/posts/{id}` | 已实现 | 删除文章 |
| GET | `/api/admin/projects` | 已实现 | 作品列表（分页，priority DESC, createdAt DESC） |
| GET | `/api/admin/projects/{id}` | 已实现 | 获取单个作品 |
| POST | `/api/admin/projects` | 已实现 | 创建作品（name/slug @NotBlank 校验） |
| PUT | `/api/admin/projects/{id}` | 已实现 | 更新作品 |
| DELETE | `/api/admin/projects/{id}` | 已实现 | 删除作品 |
| GET | `/api/admin/dashboard/stats` | 已实现 | 仪表盘统计：博客/作品/媒体计数 + 最近 5 条 |
| POST | `/api/admin/media/upload` | 已完成 | 上传图片文件（后端已实现，待前端集成） |

---

## 四、项目文档

| 文件 | 说明 |
|------|------|
| [PRD.md](PRD.md) | 产品需求文档 |
| [STATUS.md](STATUS.md) | 本文件 — 开发进度 |
| [ROUTE_MAP.md](ROUTE_MAP.md) | 英文文件名 → 前端中文显示 对照表 |
| [backend/src/main/resources/schema.sql](backend/src/main/resources/schema.sql) | 数据库 DDL 参考 |

---

## 五、用户自定义指南

### 修改博客内容
通过管理后台 `/admin` 登录后，在 `/admin/posts` 中管理文章：新建、编辑（支持 Markdown 正文）、发布、删除。无需编辑源文件。

### 修改作品内容
通过管理后台 `/admin` 登录后，在 `/admin/projects` 中管理作品：新建、编辑（支持 Markdown 详细介绍）、删除。无需编辑源文件。

### 添加图片
1. 图片放入 `frontend/public/images/`
2. 在管理后台编辑器中填写路径 `/images/文件名.jpg`，或直接填写外部 URL

### 配置项目链接 (GitHub / 演示)
直接在作品编辑器中填写 `demoUrl` 和 `sourceUrl` 字段，或编辑 [content.ts](frontend/src/lib/content.ts) 中的 `projectLinks` 作为回退

### 修改导航/品牌名
编辑 [Header.tsx](frontend/src/components/layout/Header.tsx) 和 [layout.tsx](frontend/src/app/layout.tsx) 中的 metadata

---

## 六、待实现

- [x] 管理员认证 — JWT 完整登录流程
- [x] 博客管理 CRUD — `AdminBlogController` + 前端 `PostEditor` + 文章列表/新建/编辑页面
- [x] 博客公开列表页 — 对接真实 API（`getPublishedBlogs`）
- [x] 博客公开详情页 — 对接真实 API + Markdown 渲染（`getPublishedBlogBySlug` + `MarkdownContent`）
- [x] 首页最新文章 — 对接真实 API（`getPublishedBlogs`，取前 3 篇）
- [x] 作品管理 CRUD — `AdminProjectController` + 前端 `ProjectEditor` + 作品列表/新建/编辑页面
- [x] 作品公开列表页 — 对接真实 API（`getProjects`）
- [x] 作品公开详情页 — 对接真实 API + Markdown 渲染（`getProjectBySlug` + `MarkdownContent`）
- [x] 首页精选作品 — 对接真实 API（`getProjects`，取前 2 篇）
- [x] 管理后台仪表盘 — 接入真实统计数据
- [x] 图片上传功能 — 后端实现完成 (POST /api/admin/media/upload)
- [ ] 图片上传功能 — 前端集成
- [ ] 部署上线

---

## 七、验证清单

- [x] `cd frontend && npm run dev` → localhost:3000 启动成功
- [x] 全部 8 个页面路由 200
- [x] 作品详情动态路由 `/works/lumina` `/works/fieldnotes` `/works/drift` 正常
- [x] 不存在的 slug 正确显示 404 提示
- [x] 滚动淡入动画 (FadeIn/FadeInStagger) 正常
- [x] ImageOrPlaceholder 图片加载/回退正常
- [x] `cd backend && mvn spring-boot:run` → localhost:8080 启动成功
- [x] `GET /api/blogs` → `{"code":200,"data":[]}`
- [x] `GET /api/posts` → 与 `/api/blogs` 等效
- [x] `GET /api/projects` → `{"code":200,"data":[]}`
- [x] `SHOW TABLES` → blog, project, user, media 已创建
- [x] 前后端无 CORS 错误
- [x] `POST /api/auth/admin/login` → 正确凭据返回 200 + accessToken + refreshToken + 用户信息
- [x] `POST /api/auth/admin/login` → 错误凭据返回 401 "邮箱或密码错误"
- [x] `GET /api/auth/admin/me` → 有效 token 返回 200 + 管理员信息
- [x] `GET /api/auth/admin/me` → 无 token 返回 401 "未登录或登录已过期" (JSON)
- [x] `POST /api/auth/admin/refresh` → 有效 refreshToken 返回 200 + 新 token
- [x] `POST /api/auth/admin/logout` → 成功登出，旧 refreshToken 失效
- [x] `GET /api/admin/posts` → 已登录返回分页列表；未登录返回 401
- [x] `POST /api/admin/posts` → 已登录创建文章成功
- [x] `PUT /api/admin/posts/{id}` → 已登录更新文章成功
- [x] `DELETE /api/admin/posts/{id}` → 已登录删除文章成功
- [x] `/api/admin/*` → 未登录返回 401 JSON
- [x] 前端 `/admin` → 未登录显示登录表单；登录成功显示仪表盘+用户信息+退出按钮
- [x] 前端 `/admin/posts` → 文章管理列表：查看/编辑/删除
- [x] 前端 `/admin/posts/new` → 新建文章表单，保存草稿/发布
- [x] 前端 `/admin/posts/[id]` → 编辑已有文章
- [x] 前端 `/admin/projects` → 作品管理列表：查看/编辑/删除
- [x] 前端 `/admin/projects/new` → 新建作品表单
- [x] 前端 `/admin/projects/[id]` → 编辑已有作品
- [x] 前端 `/admin` 仪表盘 → 含「作品管理」「新建作品」入口（4 卡片 2x2 网格）
- [x] `GET /api/admin/projects` → 已登录返回分页列表；未登录返回 401
- [x] `POST /api/admin/projects` → 已登录创建作品成功；缺少 name/slug 返回 400 + 中文错误
- [x] `PUT /api/admin/projects/{id}` → 已登录更新作品成功
- [x] `DELETE /api/admin/projects/{id}` → 已登录删除作品成功
- [x] 前端 `/blog` → 博客列表从 API 动态加载
- [x] 前端 `/blog/[slug]` → 博客详情从 API 加载 + Markdown 渲染
- [x] 前端 `/works` → 作品列表从 API 动态加载
- [x] 前端 `/works/[slug]` → 作品详情从 API 加载 + Markdown 渲染 + 截图展示
- [x] 前端 `/` 首页 → 精选作品从 API 加载（与博客并行请求）
- [x] 公开页面 (`/`, `/works`, `/blog`, `/about`, `/contact`) 正常运作
- [x] `GET /api/admin/dashboard/stats` → 已登录返回统计 JSON（posts/projects/media 计数 + recentPosts/recentProjects）；未登录返回 401
- [x] 前端 `/admin` 仪表盘 → 4 个统计卡片显示真实数据 + 最近文章/作品列表 + 加载/错误状态处理
