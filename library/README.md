# 图书馆管理系统启动指南

## 项目概述

本项目是一个基于 Spring Boot + MyBatis-Plus + MySQL 的图书馆管理系统后端，提供完整的图书管理、用户管理、借阅管理等功能。

## 技术栈

- **后端框架**: Spring Boot 3.5.9
- **持久层**: MyBatis-Plus 3.5.5
- **数据库**: MySQL 8.0
- **连接池**: Druid 1.2.21
- **认证授权**: JWT
- **构建工具**: Maven 3.8+

## 项目结构

```
library/
├── src/
│   ├── main/
│   │   ├── java/com/example/library/
│   │   │   ├── common/          # 通用类
│   │   │   ├── config/          # 配置类
│   │   │   ├── controller/      # 控制器层
│   │   │   ├── dto/             # 数据传输对象
│   │   │   ├── entity/          # 实体类
│   │   │   ├── mapper/          # 数据访问层
│   │   │   ├── service/         # 业务逻辑层
│   │   │   └── util/            # 工具类
│   │   └── resources/
│   │       ├── application.yml  # 应用配置
│   │       └── sql/             # SQL脚本
│   │           └── schema.sql   # 数据库初始化脚本
│   └── test/
└── pom.xml                     # Maven依赖配置
```

## 环境准备

### 1. 安装依赖

- **JDK 17+**: 确保已安装 JDK 17 或更高版本
- **MySQL 8.0+**: 确保数据库服务已启动
- **Maven 3.8+**: 用于项目构建

### 2. 数据库初始化

1. 执行数据库初始化脚本:
   ```bash
   mysql -u root -p < src/main/resources/sql/schema.sql
   ```

2. 或者手动执行 SQL 脚本:
   - 打开 MySQL 客户端
   - 执行 `src/main/resources/sql/schema.sql` 文件中的所有 SQL 语句

## 项目启动

### 1. Maven 启动方式

```bash
# 进入项目根目录
cd c:\Users\lijia\workspace\cosmic\library

# 清理并编译项目
mvn clean compile

# 启动项目
mvn spring-boot:run
```

### 2. IDE 启动方式 (以 IntelliJ IDEA 为例)

1. 打开项目
2. 确保 Maven 项目已正确加载
3. 找到 `src/main/java/com/example/library/LibraryApplication.java`
4. 右键点击该文件，选择"Run 'LibraryApplication.main()'"

### 3. 配置说明

项目使用 `application.yml` 进行配置，主要配置项:

```yaml
# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/library_management_system
    username: root
    password: DaoHaoGou123@

# JWT 配置
jwt:
  secret: library-management-system-jwt-secret-key-2026
  expiration: 7200000  # 2小时

# 服务器配置
server:
  port: 8080
```

## 接口测试

### 1. 使用 Postman 测试

1. 导入项目提供的 API 接口文档到 Postman
2. 或者手动创建请求进行测试

### 2. 基本测试流程

1. **用户登录**:
   ```
   POST http://localhost:8080/api/auth/login
   Content-Type: application/json
   
   {
     "account": "admin",
     "password": "admin"
   }
   ```

2. **获取 Token**:
   - 成功登录后，从响应中获取 JWT Token
   - 后续请求需要在 Header 中添加 `Authorization: Bearer {token}`

3. **测试其他接口**:
   - 图书管理: `GET/POST/PUT /api/books`
   - 用户管理: `GET/POST/PUT /api/users`
   - 借阅管理: `POST/GET /api/borrow`

### 3. 默认账号

- **管理员账号**:
  - 账号: `admin`
  - 密码: `admin`

## 前端对接

### 1. 跨域配置

项目已配置跨域支持，允许前端地址 `http://localhost:5173` 访问。

### 2. 接口地址

- **后端地址**: `http://localhost:8080`
- **API 路径**: `/api/*`

## 项目配置

### 1. 数据库配置

如需修改数据库连接信息，请编辑 `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://[host]:[port]/[database_name]?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: [username]
    password: [password]
```

### 2. JWT 配置

修改 JWT 相关配置:

```yaml
jwt:
  secret: [your_secret_key]
  expiration: [expiration_time_in_milliseconds]
```

### 3. 系统配置

系统运行时的默认配置:
- 教师借阅期限: 90天
- 学生借阅期限: 60天
- 单次最大借阅数: 5本

## 常见问题

### 1. 启动失败

- 检查数据库连接是否正常
- 确认数据库已创建并执行了初始化脚本
- 检查端口 8080 是否被占用

### 2. 数据库连接失败

- 确认 MySQL 服务已启动
- 检查用户名和密码是否正确
- 确认数据库名称是否正确

### 3. 接口访问失败

- 检查是否正确配置了跨域
- 确认请求头中是否包含正确的认证信息

## 项目部署

### 1. 打包

```bash
mvn clean package
```

### 2. 运行 JAR 包

```bash
java -jar target/library-0.0.1-SNAPSHOT.jar
```

## 二次开发

### 1. 代码规范

- 使用 Lombok 简化代码
- 遵循 RESTful API 设计规范
- 统一使用 Result 作为响应格式

### 2. 扩展功能

- 新增业务逻辑请在 Service 层实现
- 新增接口请在 Controller 层定义
- 数据库表结构变更请更新 Entity 和 Mapper

## 项目维护

### 1. 数据备份

定期备份数据库，确保数据安全。

### 2. 日志监控

系统日志输出到控制台，可通过日志监控系统运行状态。

### 3. 性能优化

- 使用 Druid 连接池监控数据库连接
- 通过 MyBatis-Plus 分页插件优化查询性能
- 合理配置系统参数以提高并发处理能力

---

**项目开发完成时间**: 2026-01-02
**联系方式**: Library Management System Team