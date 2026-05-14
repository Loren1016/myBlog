# MEDIA_UPLOAD_BACKEND_CHANGES.md

> 图片上传功能 — 后端实现完成（含 Code Review 修复）
>
> 日期: 2026-05-14 | 更新: 2026-05-14 (review fixes)

---

## 1. 修改摘要

实现了 `POST /api/admin/media/upload` 图片上传接口，支持管理员上传图片（封面图、文章图片、作品截图），包含文件校验、安全存储、静态资源映射和数据库记录。

---

## 2. 修改过的文件列表

| 文件 | 变更类型 | 说明 |
|------|---------|------|
| `backend/src/main/java/com/myblog/controller/MediaController.java` | 修改 | 实现 `upload()` 方法，返回 `ApiResponse<MediaUploadResponse>` |
| `backend/src/main/java/com/myblog/service/MediaService.java` | 修改 | 新增 `upload(MultipartFile)` 方法，含文件校验、安全扩展名、存储、保存、孤儿文件清理 |
| `backend/src/main/java/com/myblog/dto/MediaUploadResponse.java` | **新增** | 上传响应 DTO，含 `from(Media)` 工厂方法 |
| `backend/src/main/java/com/myblog/config/WebConfig.java` | 修改 | 新增 `/uploads/**` 静态资源映射，确保目录存在且 location URI 以 `/` 结尾 |
| `backend/src/main/java/com/myblog/exception/GlobalExceptionHandler.java` | 修改 | 新增 `MultipartException` 和 `MaxUploadSizeExceededException` 处理器 |
| `backend/src/test/java/com/myblog/service/MediaServiceTest.java` | 新增 | MediaService 单元测试 (10 tests) |
| `backend/src/test/java/com/myblog/controller/MediaControllerTest.java` | 新增 | MediaController Web 层测试 (4 tests) |

**未修改的文件：** `Media.java`、`MediaRepository.java`、`SecurityConfig.java`、`application.yml`。

---

## 3. 新增/完善的接口说明

### `POST /api/admin/media/upload`

| 项目 | 内容 |
|------|------|
| Method | `POST` |
| URL | `/api/admin/media/upload` |
| 需要认证 | 是 (Bearer Token，`/api/admin/**` 受 SecurityConfig 保护) |
| Content-Type | `multipart/form-data` |
| 表单字段 | `file` — 图片文件 |

### 成功响应 (200)

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "a1b2c3d4-e5f6-...",
    "filename": "my-photo.png",
    "url": "/uploads/a1b2c3d4-e5f6-....png",
    "size": 204800,
    "mimeType": "image/png",
    "uploadedAt": "2026-05-14T22:30:00"
  }
}
```

- `filename`: 原始上传文件名（仅用于展示）
- `url`: 图片公开访问路径（相对于后端地址，如 `http://localhost:8080/uploads/xxx.png`）
- `id`: Media 记录 UUID（与存储文件名一致）
- **注意:** 响应使用 `MediaUploadResponse` DTO，不直接暴露 `Media` Entity

### 错误响应示例

**文件为空 (400):**
```json
{ "code": 400, "message": "文件不能为空", "data": null }
```

**非图片文件 (400):**
```json
{ "code": 400, "message": "仅允许上传图片文件 (jpeg, png, gif, webp)", "data": null }
```

**未认证 (401):**
```json
{ "code": 401, "message": "未登录或登录已过期", "data": null }
```

**文件超大 (400):**
```json
{ "code": 400, "message": "上传文件大小超过限制 (最大 10MB)", "data": null }
```

---

## 4. 文件校验规则

| 检查项 | 规则 | 失败响应 |
|--------|------|---------|
| 空文件 | `file == null \|\| file.isEmpty()` | 400 "文件不能为空" |
| MIME 类型 | 仅允许 `image/jpeg`、`image/png`、`image/gif`、`image/webp` | 400 "仅允许上传图片文件..." |
| 安全扩展名 | 存储扩展名由 MIME 类型决定，**不信任原始文件名扩展名** | (由 MIME 校验覆盖) |
| 文件大小 | 单个文件 ≤ 10MB | 400 "上传文件大小超过限制..." |
| 请求大小 | 总请求 ≤ 20MB | 400 JSON 错误 |

### 安全扩展名映射

```
image/jpeg → .jpg
image/png  → .png
image/gif  → .gif
image/webp → .webp
```

攻击者上传 `payload.html` 并伪造 `Content-Type: image/png` → 存储文件为 `/uploads/<uuid>.png`，不保留 `.html` 扩展名。`filename` 字段保留原始文件名仅用于展示，不影响存储路径。

---

## 5. 上传文件保存位置和公开访问 URL 规则

### 存储规则
- **目录**: 由 `application.yml` 中 `file.upload-dir` 配置，默认 `./uploads`（相对后端运行目录）
- **统一配置**: `MediaService` 和 `WebConfig` 均通过 `@Value("${file.upload-dir:./uploads}")` 读取同一配置来源，避免写入和读取路径不一致
- **文件名**: `UUID.randomUUID() + 安全扩展名（来自 MIME 映射）`
- **自动创建**: `Files.createDirectories()` 在写入前和资源注册前均确保目录存在
- **数据库**: 每次上传生成一条 `Media` 记录
- **DB 失败清理**: 若 `mediaRepository.save()` 抛异常，已写入磁盘的文件会被 `Files.deleteIfExists()` 清理，不会残留孤儿文件。删除失败时异常作为 suppressed exception 附加到原始 DB 异常

### 静态资源映射
- **URL 模式**: `/uploads/**` → 映射到上传目录的绝对路径
- **配置位置**: `WebConfig.java` 的 `addResourceHandlers()`
- **目录 URI**: resource location 强制以 `/` 结尾（`ensureDirectoryAndGetLocation()` 确保），避免 Spring 将其当作文件路径导致 404
- **无需认证**: `/uploads/**` 不在 `/api/**` 路径下，SecurityConfig 中 `.anyRequest().permitAll()` 生效

### 完整访问示例
```
后端运行在 http://localhost:8080
上传文件存储为: ./uploads/abc123.png
公开访问 URL: http://localhost:8080/uploads/abc123.png
```

---

## 6. 前端后续集成提示

前端调用示例：

```typescript
const formData = new FormData();
formData.append("file", fileInput.files[0]);

const token = localStorage.getItem("admin_access_token");
const res = await fetch("http://localhost:8080/api/admin/media/upload", {
  method: "POST",
  headers: { Authorization: `Bearer ${token}` },
  body: formData,  // 不要设置 Content-Type，浏览器自动带 boundary
});

const json = await res.json();
// json.data.url  → "/uploads/xxx.png"
// json.data.id   → 用于后续关联
```

关键点：
- 请求体是 `FormData`，字段名 `file`
- 不要手动设置 `Content-Type: multipart/form-data`，浏览器会自动添加 `boundary`
- 需要携带 `Authorization: Bearer <token>`
- 上传成功后，将 `data.url` 填入 coverImage、screenshots 等字段
- 图片展示时拼接：`${API_BASE.replace('/api', '')}${media.url}` 或直接 `http://localhost:8080${media.url}`

---

## 7. Code Review 修复记录 (2026-05-14)

### Issue 1: 安全扩展名
- **问题**: 存储扩展名来自原始文件名，攻击者可上传 `payload.html` + 伪造 `image/png` Content-Type
- **修复**: 扩展名由校验通过的 MIME 类型决定 (`MIME_TO_EXTENSION` 映射)，不信任原始文件名
- **测试**: `upload_shouldUsePngExtension_whenOriginalFileIsHtml`、`upload_shouldUseJpgExtension_whenOriginalFileHasNoExtension`

### Issue 2: 静态资源映射 404
- **问题**: 上传目录不存在时 Java file URI 可能无结尾 `/`，Spring 误当文件路径
- **修复**: 注册资源映射前 `Files.createDirectories()`，并确保 URI 以 `/` 结尾（`ensureDirectoryAndGetLocation()` 方法）
- **测试**: `WebConfig.ensureDirectoryAndGetLocation()` 为 package-private static 方法，可单独测试

### Issue 3: 统一上传目录配置
- **问题**: `MediaService` 硬编码 `./uploads`，与 `WebConfig` 的 `file.upload-dir` 可能不一致
- **修复**: `MediaService` 通过 `@Value("${file.upload-dir:./uploads}")` 注入，与 `WebConfig` 同源
- **测试**: 测试通过 test constructor 传入自定义目录，覆盖 `@Value` 行为

### Issue 4: MediaUploadResponse DTO
- **问题**: Controller 直接返回 `ApiResponse<Media>` Entity
- **修复**: 新建 `MediaUploadResponse` DTO (`backend/src/main/java/com/myblog/dto/MediaUploadResponse.java`)，含 `from(Media)` 工厂方法。Controller 返回 `ApiResponse<MediaUploadResponse>`
- **测试**: Controller 测试改为断言 DTO 结构

### Issue 5: DB 失败孤儿文件清理
- **问题**: `save()` 在文件写入之后，失败时磁盘残留无 DB 记录的文件
- **修复**: `save()` 抛 `RuntimeException` 时 `Files.deleteIfExists()` 清理，删除失败时作为 suppressed exception
- **测试**: `upload_shouldCleanUpFile_whenDbSaveFails`

---

## 8. 测试与验证命令，以及实际结果

### 目标测试

```bash
cd backend
mvn test -Dtest=MediaServiceTest,MediaControllerTest
```

**实际结果 (2026-05-14 23:01):**
```
Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

- **MediaServiceTest**: 10 tests pass
  - `upload_shouldSaveFileToDiskAndReturnDto` — 文件写入磁盘 + DB 保存，返回 DTO
  - `upload_shouldGenerateUniqueStoredPath` — UUID 文件名
  - `upload_shouldThrowOnEmptyFile` — 空文件抛异常
  - `upload_shouldThrowOnNullFile` — null 文件抛异常
  - `upload_shouldThrowOnNonImageType` — PDF 被拒绝
  - `upload_shouldRejectTextPlainType` — text/plain 被拒绝
  - `upload_shouldAcceptAllAllowedImageTypes` — 4 种图片类型全通过
  - `upload_shouldUsePngExtension_whenOriginalFileIsHtml` — HTML 伪造 → .png
  - `upload_shouldUseJpgExtension_whenOriginalFileHasNoExtension` — 无扩展名 → .jpg
  - `upload_shouldCleanUpFile_whenDbSaveFails` — DB 失败清理孤儿文件

- **MediaControllerTest**: 4 tests pass
  - `upload_shouldReturn200_withCorrectStructure` — 验证 DTO JSON 结构
  - `upload_shouldReturn400_whenEmptyFile` — 空文件 400
  - `upload_shouldReturn400_whenNonImageType` — 非图片 400
  - `upload_shouldAcceptWebpImage` — webp 通过

### 完整回归测试

```bash
cd backend
mvn test
```

**实际结果 (2026-05-14 23:02):**
```
Tests run: 20, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

所有已有测试无回归 (AdminDashboardControllerTest x2, DashboardServiceTest x3, MyBlogApplicationTests x1)。
