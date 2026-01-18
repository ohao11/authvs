# RBAC权限管理系统使用指南

## 📋 系统概述

本系统实现了完整的基于角色的访问控制（RBAC - Role-Based Access Control）权限管理，支持**后台管理员**和**门户用户**两种用户类型，提供模块级别的权限控制。

## 🏗️ 数据库设计

### 核心表结构

系统包含5张核心表：

1. **users** - 用户表
   - 字段：id, username, password, email, phone, user_type, enabled, created_at, updated_at
   - user_type：1=门户用户，2=后台管理员
   - 唯一索引：username, email, phone

2. **roles** - 角色表
   - 字段：id, role_name, role_code, role_type, description, enabled, created_at, updated_at
   - role_type：1=门户角色，2=后台角色

3. **permissions** - 权限表（模块级）
   - 字段：id, permission_name, permission_code, permission_type, module_path, parent_id, sort_order, description, enabled, created_at, updated_at
   - permission_type：1=门户权限，2=后台权限

4. **user_roles** - 用户角色关联表
   - 字段：id, user_id, role_id, created_at
   - 外键关联users和roles表

5. **role_permissions** - 角色权限关联表
   - 字段：id, role_id, permission_id, created_at
   - 外键关联roles和permissions表

### 数据库初始化

执行 `src/main/resources/rbac_init.sql` 脚本来创建表结构和初始化示例数据：

```bash
mysql -h localhost -u root -p authvs < src/main/resources/rbac_init.sql
```

## 🧪 测试账号

系统初始化了6个测试账号：

### 后台管理员（user_type=2）

| 用户名 | 密码 | 角色 | 权限范围 |
|--------|------|------|----------|
| admin | admin123 | 超级管理员 | 全部后台权限（用户、角色、权限、内容、系统管理） |
| operator | operator123 | 运营人员 | 用户管理、内容管理 |
| auditor | auditor123 | 审核人员 | 内容管理 |

### 门户用户（user_type=1）

| 用户名 | 密码 | 角色 | 权限范围 |
|--------|------|------|----------|
| vip_user | vip123 | VIP会员 | 全部门户权限（个人中心、订单、会员服务、消息） |
| normal_user | user123 | 普通会员 | 个人中心、订单 |
| guest_user | guest123 | 访客 | 个人中心 |

## 🔑 权限格式

Spring Security的权限格式：

- **角色权限**：`ROLE_角色编码`
  - 例如：`ROLE_SUPER_ADMIN`, `ROLE_VIP_MEMBER`

- **模块权限**：`PERM_权限编码`
  - 例如：`PERM_USER_MODULE`, `PERM_ORDER_MODULE`

## 📡 API接口

### 1. 登录认证

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

响应：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  }
}
```

### 2. 获取当前用户基本信息

```http
GET /api/users/me
Authorization: Bearer {token}
```

响应：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "username": "admin",
    "roles": [
      "ROLE_SUPER_ADMIN",
      "PERM_USER_MODULE",
      "PERM_ROLE_MODULE",
      "PERM_PERMISSION_MODULE",
      "PERM_CONTENT_MODULE",
      "PERM_SYSTEM_MODULE"
    ]
  }
}
```

### 3. 获取当前用户详细信息（包含RBAC）

```http
GET /api/users/me/detail
Authorization: Bearer {token}
```

响应：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com",
    "phone": "13800138000",
    "userType": 2,
    "enabled": true,
    "roles": [
      {
        "id": 1,
        "roleName": "超级管理员",
        "roleCode": "SUPER_ADMIN",
        "roleType": 2,
        "description": "后台超级管理员，拥有所有权限"
      }
    ],
    "permissions": [
      {
        "id": 1,
        "permissionName": "用户管理",
        "permissionCode": "USER_MODULE",
        "permissionType": 2,
        "modulePath": "/admin/users",
        "description": "后台用户管理模块"
      },
      {
        "id": 2,
        "permissionName": "角色管理",
        "permissionCode": "ROLE_MODULE",
        "permissionType": 2,
        "modulePath": "/admin/roles",
        "description": "后台角色管理模块"
      }
      // ... 其他权限
    ]
  }
}
```

## 🔒 权限控制

### 在Controller中使用

```java
@PreAuthorize("hasRole('SUPER_ADMIN')")
@GetMapping("/admin/users")
public ResultDTO<List<UserVo>> listUsers() {
    // 只有超级管理员可以访问
}

@PreAuthorize("hasAuthority('PERM_USER_MODULE')")
@GetMapping("/admin/user-module")
public ResultDTO<?> userModule() {
    // 拥有用户管理模块权限的用户可以访问
}

@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATOR')")
@PostMapping("/admin/content")
public ResultDTO<?> createContent() {
    // 超级管理员或运营人员可以访问
}
```

### 在Service中使用

```java
@Service
public class UserManagementService {
    
    @PreAuthorize("hasAuthority('PERM_USER_MODULE')")
    public void createUser(User user) {
        // 需要用户管理模块权限
    }
}
```

## 🚀 测试步骤

### 1. 启动应用

```bash
mvn spring-boot:run
```

### 2. 访问Swagger UI

打开浏览器访问：http://localhost:8080/swagger-ui.html

### 3. 测试流程

1. **登录获取Token**
   - 使用 `/api/auth/login` 接口
   - 用测试账号登录（如admin/admin123）
   - 获取JWT token

2. **测试基本信息接口**
   - 使用 `/api/users/me` 接口
   - 在请求头添加：`Authorization: Bearer {token}`
   - 查看返回的角色和权限列表

3. **测试详细信息接口**
   - 使用 `/api/users/me/detail` 接口
   - 查看完整的RBAC信息（角色详情、权限详情）

4. **测试不同用户类型**
   - 分别使用后台管理员和门户用户登录
   - 观察不同用户的角色和权限差异

## 📊 系统架构

```
┌─────────────────┐
│  Controller层   │  (接收请求，权限注解)
└────────┬────────┘
         │
┌────────▼────────┐
│   Service层     │  (业务逻辑，UserService)
└────────┬────────┘
         │
┌────────▼────────┐
│   Mapper层      │  (数据访问，MyBatis-Plus)
└────────┬────────┘
         │
┌────────▼────────┐
│  Database层     │  (MySQL RBAC表)
└─────────────────┘
```

## 🔧 核心组件

### 1. CustomUserDetailsService

负责加载用户信息和RBAC权限：
- 查询用户基本信息
- 通过user_roles和role_permissions表联查角色和权限
- 构建Spring Security的GrantedAuthority列表

### 2. UserService

提供用户相关业务逻辑：
- 根据用户名获取完整的用户详情
- 包含角色信息和权限信息的组装

### 3. Entity类

- `User`: 用户实体
- `Role`: 角色实体
- `Permission`: 权限实体
- `UserRole`: 用户-角色关联
- `RolePermission`: 角色-权限关联

### 4. Mapper接口

- `UserMapper`: 用户数据访问
- `RoleMapper`: 角色数据访问
- `PermissionMapper`: 权限数据访问
- `UserRoleMapper`: 用户-角色关联数据访问
- `RolePermissionMapper`: 角色-权限关联数据访问

## 📝 扩展建议

### 1. 添加权限管理接口

创建Controller管理角色和权限：
- 创建/修改/删除角色
- 分配权限给角色
- 分配角色给用户

### 2. 添加数据权限

除了模块级权限，还可以实现：
- 行级数据权限（例如：只能看到自己部门的数据）
- 字段级权限（例如：某些敏感字段只有特定角色可见）

### 3. 权限缓存

对于高并发场景，可以使用Redis缓存用户的权限信息：
```java
@Cacheable(value = "user:permissions", key = "#username")
public UserDetails loadUserByUsername(String username) {
    // ...
}
```

### 4. 审计日志

记录用户的权限变更和敏感操作：
- 角色分配记录
- 权限修改记录
- 敏感操作日志

## ⚠️ 注意事项

1. **密码安全**：示例数据中的密码已使用BCrypt加密
2. **唯一性约束**：username、email、phone都有唯一索引
3. **级联删除**：user_roles和role_permissions配置了外键级联删除
4. **权限检查**：建议在Controller层使用@PreAuthorize注解进行权限检查
5. **类型区分**：注意区分user_type、role_type、permission_type (1=门户, 2=后台)

## 🔗 相关文档

- [Spring Security官方文档](https://spring.io/projects/spring-security)
- [MyBatis-Plus官方文档](https://baomidou.com/)
- [JWT Token规范](https://jwt.io/)
