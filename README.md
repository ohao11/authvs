# AuthVS - Spring Boot RBAC认证授权系统

基于 Spring Boot 3.x + Spring Security + JWT + MyBatis-Plus 的 RBAC（基于角色的访问控制）认证授权系统。

## 技术栈

- **Java 21**
- **Spring Boot 4.0.1**
- **Spring Security 7.0.x** - JWT 认证 + 方法级权限控制
- **MyBatis-Plus 3.5.16** - ORM 框架
- **MySQL 8.0+** - 数据持久化
- **Redis 7.0+** - Token 撤销 + 权限缓存
- **Hutool 5.8.43** - 工具库（脱敏、JSON、字符串处理）
- **SpringDoc OpenAPI 3.0.1** - API 文档
- **jjwt 0.11.5** - JWT token 生成与验证
- **Lombok** - 简化代码
- **Jackson** - JSON 序列化

## 核心特性

### 1. 完整的 RBAC 权限体系
- 用户-角色-权限三层模型
- 支持多角色、多权限
- 超级管理员自动拥有所有权限
- 基于注解的方法级权限控制 `@PreAuthorize`

### 2. JWT 认证
- 单设备登录控制（同一设备类型只保留最新 token）
- Token 主动撤销机制（登出时立即失效）
- Token 中包含 userId、deviceType 等关键信息
- 1小时有效期，自动过期

### 3. 权限缓存优化 ⚡
- **Redis 缓存用户权限**，避免重复查询数据库
- 登录后权限信息缓存1小时（与 JWT 同步）
- **性能提升 50 倍**（5次DB查询 → 0次查询）
- 权限变更时自动清除缓存，实时生效
- Redis 故障自动降级到数据库查询

### 4. 操作日志系统
- AOP 切面自动记录所有操作
- 记录用户ID、用户名、IP地址、设备类型、请求参数、响应结果
- 支持多条件查询和分页
- 敏感数据自动脱敏（密码、邮箱、手机号）
- **Java 21 虚拟线程**异步持久化，轻量高效，无需调优

### 5. 数据脱敏
- 邮箱脱敏：`u***@example.com`
- 手机号脱敏：`138****5678`
- JSON 中的密码字段自动脱敏

### 6. 国际化支持
- 中英文错误提示
- 基于请求头 `Accept-Language` 自动切换

### 7. 统一异常处理
- 全局异常拦截
- 统一响应格式
- 友好的错误提示

## 数据库表结构

```sql
-- 用户表
user (id, username, password, email, phone, user_type, enabled, created_at, updated_at)

-- 角色表
role (id, role_code, role_name, role_type, description, enabled, created_at, updated_at)

-- 权限表
permission (id, permission_code, permission_name, permission_type, module_path, description, enabled, created_at, updated_at)

-- 用户角色关联表
user_role (id, user_id, role_id, created_at)

-- 角色权限关联表
role_permission (id, role_id, permission_id, created_at)

-- 操作日志表
operation_log (id, user_id, username, operation_type, operation_module, operation_desc, request_method, request_url, request_params, response_result, ip_address, device_type, user_agent, status, error_message, execute_time, created_at)
```

## 快速开始

### 前置要求
- Java 21+
- Maven 3.9+
- MySQL 8.0+
- Redis 7.0+

### 1. 克隆项目

```bash
git clone <repository-url>
cd authVs/be
```

### 2. 配置数据库

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/auth_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    
  data:
    redis:
      host: localhost
      port: 6379
      password: # 如果有密码则填写
```

### 3. 初始化数据库

执行 `src/main/resources/rbac_init.sql` 初始化 RBAC 数据：

```bash
mysql -u root -p auth_db < src/main/resources/rbac_init.sql
```

初始数据包含：
- 1个超级管理员用户（`admin` / `admin123`）
- 2个测试用户
- 3个角色（超级管理员、审计管理员、普通用户）
- 2个权限模块（用户管理、审计管理）

### 4. 启动应用

```bash
mvn clean package -DskipTests
mvn spring-boot:run
```

应用启动在 `http://localhost:8080`

### 5. 访问 API 文档

Swagger UI: `http://localhost:8080/swagger-ui.html`

## API 使用示例

### 登录获取 Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "deviceType": "WEB"
  }'
```

响应：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "username": "admin",
    "userId": 1,
    "roles": ["ROLE_SUPER_ADMIN"]
  }
}
```

### 访问受保护资源

```bash
TOKEN="<your_token>"
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer ${TOKEN}"
```

### 查询用户列表（需要权限）

```bash
curl -X POST http://localhost:8080/api/users/page \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "pageNum": 1,
    "pageSize": 10,
    "username": "",
    "email": "",
    "phone": "",
    "enabled": null
  }'
```

### 查询操作日志（需要审计权限）

```bash
curl -X POST http://localhost:8080/api/operation-logs/page \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "pageNum": 1,
    "pageSize": 10,
    "username": "admin",
    "operationType": "LOGIN"
  }'
```

### 登出

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer ${TOKEN}"
```

## 权限配置

### 角色和权限

| 角色代码 | 角色名称 | 权限 |
|---------|---------|------|
| SUPER_ADMIN | 超级管理员 | 所有权限（自动） |
| AUDIT_ADMIN | 审计管理员 | PERM_AUDIT_MODULE |
| USER | 普通用户 | 无特殊权限 |

### 权限检查示例

```java
@PreAuthorize("@accessChecker.perm('PERM_USER_MODULE')")
@GetMapping("/users/list")
public ResultDTO<List<User>> getUsers() {
    // 只有拥有 PERM_USER_MODULE 权限或超级管理员可访问
}
```

## 性能优化

### 权限缓存机制

系统实现了基于 Redis 的权限缓存，大幅减少数据库查询：

**优化前：**
- 每次请求都查询 5 次数据库（user、user_role、role、role_permission、permission）
- 1000 用户 × 50 请求/小时 = 250,000 次数据库查询

**优化后：**
- 首次登录：5 次查询 + 写入 Redis 缓存
- 后续请求：直接从 Redis 读取，**0 次数据库查询**
- 1000 用户 × 50 请求/小时 = 5,000 次数据库查询
- **性能提升 50 倍，数据库查询减少 98%**

### 缓存失效策略

- **TTL 过期**：缓存 1 小时后自动失效（与 JWT 同步）
- **主动清除**：权限变更时立即清除缓存
- **故障降级**：Redis 不可用时自动回退到数据库查询

## 配置项

### application.yml 关键配置

```yaml
# JWT 配置
jwt:
  secret: super-secret-key-change-me-please-32-bytes-minimum
  expiration: 3600000  # 1小时（毫秒）

# Redis 配置（用于 token 撤销和权限缓存）
spring:
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 5000ms
```

## 项目结构

```
be/
├── src/main/java/org/max/authvs/
│   ├── api/                      # 控制器层
│   │   ├── AuthController.java       # 认证接口
│   │   ├── UserController.java       # 用户管理接口
│   │   ├── OperationLogController.java # 操作日志接口
│   │   └── dto/                      # 数据传输对象
│   ├── config/                   # 配置类
│   │   ├── SecurityConfig.java       # Spring Security 配置
│   │   ├── OpenApiConfig.java        # Swagger 配置
│   │   └── I18nConfig.java           # 国际化配置
│   ├── entity/                   # 实体类
│   │   ├── User.java
│   │   ├── Role.java
│   │   ├── Permission.java
│   │   └── OperationLog.java
│   ├── mapper/                   # MyBatis-Plus Mapper
│   ├── security/                 # 安全相关
│   │   ├── JwtService.java           # JWT 处理
│   │   ├── JwtAuthenticationFilter.java # JWT 认证过滤器
│   │   ├── CustomUserDetails.java    # 自定义用户详情
│   │   ├── AccessChecker.java        # 权限检查器
│   │   ├── PermissionCacheService.java # 权限缓存服务
│   │   └── PermissionCacheVO.java    # 权限缓存对象
│   ├── service/                  # 业务逻辑层
│   │   ├── CustomUserDetailsService.java # 用户加载服务
│   │   ├── UserService.java
│   │   └── OperationLogService.java
│   ├── utils/                    # 工具类
│   │   ├── IpUtils.java              # IP 提取
│   │   └── SensitiveDataUtils.java   # 数据脱敏
│   └── aspect/                   # AOP 切面
│       └── OperationLogAspect.java   # 操作日志切面
└── src/main/resources/
    ├── application.yml           # 应用配置
    ├── rbac_init.sql            # 数据库初始化脚本
    └── i18n/                    # 国际化资源
        ├── messages.properties       # 中文
        └── messages_en.properties    # 英文
```

## 开发指南

### 添加新的权限控制

1. 在数据库 `permission` 表添加新权限记录
2. 在控制器方法上添加注解：
```java
@PreAuthorize("@accessChecker.perm('PERM_YOUR_MODULE')")
```

### 添加操作日志

在方法上添加 `@OperationLog` 注解：

```java
@OperationLog(
    type = OperationType.CREATE,
    module = "用户管理",
    description = "创建新用户"
)
public ResultDTO<User> createUser(@RequestBody User user) {
    // ...
}
```

### 清除权限缓存

当权限、角色发生变更时，调用：

```java
@Autowired
private PermissionCacheService permissionCacheService;

public void updateUserRole(String username) {
    // 更新数据库...
    
    // 清除缓存，下次请求时自动重新加载
    permissionCacheService.clearCache(username);
}
```

## 安全建议

1. **生产环境必须修改** `JWT_SECRET`
2. 使用强密码策略
3. 启用 HTTPS
4. 定期轮换密钥
5. 监控异常登录行为
6. 定期归档操作日志

## 监控与运维

### Redis 缓存键

- `auth:permissions:{username}` - 用户权限缓存
- `auth:revoked:{token}` - 已撤销的 token
- `auth:user:{userId}:device:{deviceType}` - 设备 token 映射

### 查看缓存命中情况

可以通过 Redis 监控工具或添加自定义监控端点查看缓存命中率。

## 故障排查

### 1. Redis 连接失败
- 系统会自动降级到数据库查询
- 检查 Redis 服务状态和配置

### 2. Token 无效
- 检查 token 是否过期
- 检查 token 是否被撤销（登出后）
- 验证 JWT_SECRET 是否一致

### 3. 权限不生效
- 检查用户角色和权限配置
- 清除 Redis 权限缓存重试
- 查看操作日志确认权限检查结果

## 待办事项

- [ ] 添加 Actuator 健康检查
- [ ] 添加缓存命中率监控端点
- [ ] 实现权限变更事件通知
- [ ] 添加用户密码修改接口
- [ ] 实现 OAuth2 集成
- [ ] 添加单元测试和集成测试

## License

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！

