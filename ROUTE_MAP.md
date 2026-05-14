# 英文文件 → 前端中文显示 对照表

> 文件夹/文件名保持英文，前端用户看到的是中文。

---

## 路由对照

| 英文路由 (文件夹) | 中文页面名 |
|-------------------|------------|
| `/` (page.tsx) | 首页 |
| `/works` (works/) | 作品 |
| `/works/[slug]` (works/[slug]/) | 作品详情 |
| `/blog` (blog/) | 博客 |
| `/blog/[slug]` (blog/[slug]/) | 文章详情 |
| `/about` (about/) | 关于 |
| `/contact` (contact/) | 联系 |
| `/admin` (admin/) | 管理后台 |

## 导航对照

| 英文 (link) | 中文显示 |
|-------------|----------|
| Works | 作品 |
| Journal | 博客 |
| About | 关于 |
| Contact | 联系 |

## 作品 (Works) 对照

| 英文 slug | 中文名 | 一句话简介 (中文) |
|-----------|--------|-------------------|
| lumina | 流光 | 一个探索光影、形态与算法美学的生成艺术平台 |
| fieldnotes | 野记 | 为在意字体排印与阅读体验的写作者打造的极简内容管理系统 |
| drift | 漂移 | 使用实地录音与程序化音频生成环境音景的生成器 |
| threadbare | 素线 | 面向长文写作的极简协作书写工具 |
| index | 索引 | 支持双向链接与图谱可视化的个人知识库 |

## 博客 (Blog) 对照

| 英文 slug | 中文标题 |
|-----------|----------|
| on-craft | 论手艺：关于软件作为一门技艺的思考 |
| quiet-engineering | 静默工程：构建不喧宾夺主的系统 |
| typography-for-developers | 开发者字体排印指南 |
| the-art-of-abstraction | 抽象的艺术：何时构建，何时借用 |
| working-with-constraints | 在约束中创造：有限空间中的创造力 |

## 分类/标签对照

| 英文 | 中文 |
|------|------|
| Process | 方法论 |
| Engineering | 工程 |
| Design | 设计 |
| Tools | 工具 |
| Reflection | 反思 |
| Full-Stack | 全栈 |
| Tool | 工具 |

## 页面模块对照

| 英文组件/文件 | 前端显示 |
|--------------|----------|
| Header.tsx | 顶部导航栏 |
| Footer.tsx | 底部版权与社交链接 |
| SectionHeading.tsx | 章节标题组件 |
| FadeIn.tsx | 滚动淡入动画 |
| ImageOrPlaceholder.tsx | 图片/占位图组件 |
| Container.tsx | 页面宽度容器 |
| lib/api.ts | 后端 API 调用封装 |
| lib/content.ts | 图片 & 内容映射配置 |
| lib/utils.ts | 工具函数 |
| types/blog.ts | 博客类型定义 |
| types/project.ts | 作品类型定义 |

## 后端 API 对照

| 接口路径 | 说明 |
|----------|------|
| GET /api/blogs | 获取博客列表 |
| GET /api/blogs/{slug} | 获取单篇博客 |
| GET /api/projects | 获取作品列表 |
| GET /api/projects/{slug} | 获取单个作品 |
| POST /api/auth/login | 管理员登录 |
| POST /api/admin/blogs | 创建博客 (需认证) |
| PUT /api/admin/blogs/{id} | 更新博客 (需认证) |
| DELETE /api/admin/blogs/{id} | 删除博客 (需认证) |
| POST /api/admin/projects | 创建作品 (需认证) |
| PUT /api/admin/projects/{id} | 更新作品 (需认证) |
| DELETE /api/admin/projects/{id} | 删除作品 (需认证) |
| POST /api/admin/media/upload | 上传媒体文件 (需认证) |

## 数据库表对照

| 英文表名 | 中文说明 |
|----------|----------|
| blog | 博客文章表 |
| project | 作品项目表 |
| user | 管理员用户表 |
| media | 媒体文件表 |
