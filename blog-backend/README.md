# Blog Backend

这是一个使用 Spring Boot 3.2.3 构建的博客系统后端。

## 技术栈

- Spring Boot 3.2.3
- Spring Security
- JWT 认证
- MyBatis-Plus
- SQL Server
- Lombok

## 项目结构

```
src/main/java/com/example/blogbackend/
├── config/                 # 配置类
│   ├── ApplicationConfig.java
│   └── SecurityConfig.java
├── controller/            # 控制器
│   └── AuthenticationController.java
├── dto/                   # 数据传输对象
│   ├── AuthenticationRequest.java
│   ├── AuthenticationResponse.java
│   └── RegisterRequest.java
├── entity/               # 实体类
│   └── User.java
├── repository/           # 数据访问层
│   └── UserRepository.java
├── security/             # 安全相关
│   └── JwtAuthenticationFilter.java
├── service/              # 服务层
│   ├── AuthenticationService.java
│   └── JwtService.java
└── BlogBackendApplication.java
```

## 功能特性

1. 用户认证
   - 用户注册
   - 用户登录
   - JWT 令牌生成和验证

2. 安全性
   - 密码加密存储
   - 基于 JWT 的无状态认证
   - 角色基础的访问控制

## API 端点

### 认证 API

1. 注册
   ```
   POST /api/auth/register
   Content-Type: application/json

   {
     "email": "user@example.com",
     "password": "password",
     "nickname": "nickname"
   }
   ```

2. 登录
   ```
   POST /api/auth/authenticate
   Content-Type: application/json

   {
     "email": "user@example.com",
     "password": "password"
   }
   ```

## 配置

在 `application.yml` 中配置以下内容：

1. 数据库连接
   ```yaml
   spring:
     datasource:
       url: jdbc:sqlserver://localhost:1433;databaseName=blog;encrypt=true;trustServerCertificate=true
       username: sa
       password: YourStrong@Passw0rd
   ```

2. JWT 配置
   ```yaml
   jwt:
     secret-key: your-secret-key
     expiration: 86400000  # 24小时
   ```

## 开发环境要求

- JDK 17 或更高版本
- SQL Server 2019 或更高版本
- Maven 3.6 或更高版本

## 构建和运行

1. 克隆项目
   ```bash
   git clone https://github.com/yourusername/blog-backend.git
   cd blog-backend
   ```

2. 构建项目
   ```bash
   mvn clean package
   ```

3. 运行项目
   ```bash
   java -jar target/blog-backend-1.0.0.jar
   ```

## 数据库表结构

### users 表
```sql
CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(255),
    role VARCHAR(50) NOT NULL
);
``` 