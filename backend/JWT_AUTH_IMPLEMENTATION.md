# 管理员 JWT 认证 — 实现说明

> 完成日期: 2026-05-12

---

## 1. 修改文件清单

### 新增 6 个文件

| 文件 | 说明 |
|------|------|
| `security/JwtProvider.java` | JWT 生成、解析、校验 |
| `security/JwtAuthenticationFilter.java` | 请求拦截，从 Authorization header 提取 token 注入 SecurityContext |
| `dto/LoginResponse.java` | 登录响应（accessToken + refreshToken + 用户信息） |
| `dto/RefreshRequest.java` | 刷新请求（refreshToken） |
| `dto/AdminUserDto.java` | 管理员信息 DTO（不含密码） |
| `config/DataInitializer.java` | 启动时自动创建默认管理员 |

### 修改 5 个文件

| 文件 | 变更内容 |
|------|----------|
| `entity/User.java` | 增加 `role` 和 `tokenVersion` 字段 |
| `service/AuthService.java` | 完整实现 login / refresh / logout / getMe |
| `controller/AuthController.java` | 四个接口：`login`、`refresh`、`logout`、`me` |
| `config/SecurityConfig.java` | 注册 JWT filter、401/403 JSON 响应、端点权限规则 |
| `exception/GlobalExceptionHandler.java` | 增加 `BadCredentialsException`（401）和 `MethodArgumentNotValidException`（400）处理 |

### 未修改的文件

BlogController、ProjectController、MediaController、BlogService、ProjectService、MediaService、所有 Repository — 全部保持原样。

---

## 2. 登录流程

```
POST /api/auth/admin/login  { email, password }
  → AuthService.login()
    → UserRepository.findByEmail()
    → BCrypt.matches(password, user.passwordHash)
    → JwtProvider.generateAccessToken() + generateRefreshToken()
  ← { accessToken, refreshToken, user }

后续请求: Authorization: Bearer <accessToken>
  → JwtAuthenticationFilter.doFilterInternal()
    → 提取 token → 校验 → 查用户 → 注入 SecurityContext
  → Controller 处理
```

### 刷新流程

```
POST /api/auth/admin/refresh  { refreshToken }
  → 校验 JWT 签名和有效期
  → 比对 token 中的 tokenVersion 与数据库中 User.tokenVersion
  → 一致则签发新 accessToken + refreshToken
  → 不一致则 401 "refresh token 已被撤销"
```

### 登出流程

```
POST /api/auth/admin/logout  (需 Authorization header)
  → User.tokenVersion += 1
  → 保存到数据库
  → 旧 refresh token 立即失效（tokenVersion 不匹配）
```

---

## 3. Token 存放方式

- **accessToken**：JWT，7 天有效期（`app.jwt-expiration-ms=604800000`），包含 userId、email、role
- **refreshToken**：JWT，28 天有效期（4 倍 access），包含 userId、tokenVersion
- 均在响应 JSON body 中返回，客户端自行存储
- **撤销机制**：logout 时 `User.tokenVersion` 自增，已签发的 refresh token 立即失效；access token 无法主动撤销（JWT 固有取舍），但有效期短，风险可控

---

## 4. 接口说明

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/auth/admin/login` | 无需 | 管理员登录，返回 accessToken + refreshToken + 用户信息 |
| POST | `/api/auth/admin/refresh` | 无需 | 刷新 token，入参 refreshToken |
| POST | `/api/auth/admin/logout` | 需 | 退出登录，撤销 refresh token |
| GET | `/api/auth/admin/me` | 需 | 获取当前登录管理员信息 |

**登录请求示例：**

```json
{
  "email": "admin@myblog.com",
  "password": "admin123"
}
```

**登录成功响应：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": "56710833-...",
      "email": "admin@myblog.com",
      "displayName": "管理员",
      "avatar": null,
      "role": "ADMIN"
    }
  }
}
```

**错误响应格式（统一）：**

```json
{
  "code": 401,
  "message": "未登录或登录已过期",
  "data": null
}
```

---

## 5. 安全规则

| 路径 | 权限 |
|------|------|
| `/api/auth/admin/login` | 公开 |
| `/api/auth/admin/refresh` | 公开 |
| `/api/auth/**` | 需认证 |
| `/api/blogs/**` | 公开 |
| `/api/projects/**` | 公开 |
| `/api/admin/**` | 需认证 |
| 其他 | 公开 |

- 密码 BCrypt 加密存储
- 无状态 Session（`STATELESS`）
- CSRF 已关闭（API 无 Cookie）
- 未登录返回 `401 {"code":401,"message":"未登录或登录已过期"}`
- 权限不足返回 `403 {"code":403,"message":"权限不足"}`

---

## 6. 本地验证

```bash
# 要求：MySQL 已启动且 myblog 数据库存在
cd backend
mvn spring-boot:run

# 另开终端执行：

# 1. 登录
curl -s -X POST http://localhost:8080/api/auth/admin/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@myblog.com","password":"admin123"}'

# 2. 获取当前用户（用返回的 accessToken 替换 <token>）
curl -s http://localhost:8080/api/auth/admin/me \
  -H "Authorization: Bearer <token>"

# 3. 刷新 token
curl -s -X POST http://localhost:8080/api/auth/admin/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refreshToken>"}'

# 4. 退出登录
curl -s -X POST http://localhost:8080/api/auth/admin/logout \
  -H "Authorization: Bearer <token>"

# 5. 未登录访问受保护接口 → 401
curl -s http://localhost:8080/api/admin/blogs
```

### 验证通过的 10 个场景

| # | 场景 | 预期 | 结果 |
|---|------|------|------|
| 1 | 正确凭据登录 | 200 + token | ✅ |
| 2 | 错误密码登录 | 401 "邮箱或密码错误" | ✅ |
| 3 | 有效 token 访问 /me | 200 + 用户信息 | ✅ |
| 4 | 无 token 访问 /me | 401 JSON | ✅ |
| 5 | 有效 refreshToken 刷新 | 200 + 新 token | ✅ |
| 6 | 已登录用户 logout | 200 | ✅ |
| 7 | 已登出后旧 refreshToken 刷新 | 401 "已被撤销" | ✅ |
| 8 | 无效 token 访问 /me | 401 JSON | ✅ |
| 9 | 未登录访问 /api/admin/* | 401 JSON | ✅ |
| 10 | 参数校验失败 | 400 + 字段错误 | ✅ |

---

## 7. 默认管理员种子数据

`DataInitializer` 在启动时检查 `admin@myblog.com` 是否存在，不存在则自动创建。

| 字段 | 值 |
|------|-----|
| email | `admin@myblog.com` |
| password | `admin123` |
| displayName | 管理员 |
| role | ADMIN |

密码经 BCrypt 加密存储，数据库中不保存明文。

---

## 8. 可选后续增强

- access token 有效期可缩短至 15–30 分钟，降低不可撤销的风险窗口
- 对敏感操作（删除博客等）可在 Controller 层做二次 `role` 校验
- 增加登录失败次数限制（防暴力破解）
- refresh token 可改为存储在数据库，支持精细撤销（按设备/按会话）
- `app.jwt-secret` 建议在生产环境改为强随机密钥（如 `openssl rand -base64 64`）
- 前端 login 页面和 token 管理（按需求说明已限定不做前端改动，此处仅列示）
