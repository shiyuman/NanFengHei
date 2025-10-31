
# 开发规范指南

为保证代码质量、可维护性、安全性与可扩展性，请在开发过程中严格遵循以下规范。

## 一、项目基本信息

- **用户工作目录**：`C:\Users\user\NanFengHei`
- **项目名称**：NanFengHei
- **开发者**：user
- **操作系统**：Windows 10
- **当前时间**：2025-10-31 09:38:17

### 目录结构

```
NanFengHei
├── sql
└── src
    ├── main
    │   ├── java
    │   │   └── cn
    │   │       └── sym
    │   │           ├── common
    │   │           │   ├── constant
    │   │           │   ├── exception
    │   │           │   └── response
    │   │           ├── config
    │   │           ├── controller
    │   │           ├── dto
    │   │           ├── entity
    │   │           ├── repository
    │   │           ├── service
    │   │           │   └── impl
    │   │           └── utils
    │   └── resources
    └── test
        └── java
            └── cn
                └── sym
                    └── service
                        └── impl
```

## 二、技术栈要求

- **主框架**：Spring Boot 2.7.0（基于 Spring Boot Starter Parent）
- **语言版本**：Java 11
- **构建工具**：Maven
- **SDK 版本**：
  - JDK 25
  - MySQL Connector Java 8.x
  - MyBatis Spring Boot Starter 2.2.0
  - Lombok
  - JWT (jjwt 0.9.1)
  - Swagger UI & Swagger Core 2.9.2
  - Redis Spring Boot Starter
  - WebSocket Spring Boot Starter
  - Aliyun SDK for OSS 和 Core

## 三、分层架构规范

| 层级        | 职责说明                         | 开发约束与注意事项                                               |
|-------------|----------------------------------|----------------------------------------------------------------|
| **Controller** | 处理 HTTP 请求与响应，定义 API 接口 | 不得直接访问数据库，必须通过 Service 层调用                  |
| **Service**    | 实现业务逻辑、事务管理与数据校验   | 必须通过 Repository 或 Mapper 访问数据库；返回 DTO 而非 Entity（除非必要） |
| **Repository** | 数据库访问与持久化操作             | 继承 `JpaRepository` 或使用 MyBatis Mapper；避免 N+1 查询问题     |
| **Entity**     | 映射数据库表结构                   | 不得直接返回给前端（需转换为 DTO）；包名统一为 `entity`         |

### 接口与实现分离

- 所有接口实现类需放在接口所在包下的 `impl` 子包中。
- 如 `UserService` 对应的实现类是 `UserServiceImpl`，位于 `service.impl` 包下。

## 四、安全与性能规范

### 输入校验

- 使用 `@Valid` 与 JSR-303 校验注解（如 `@NotBlank`, `@Size` 等）
  - 注意：Spring Boot 2.7 中校验注解仍位于 `javax.validation.constraints.*`

- 禁止手动拼接 SQL 字符串，防止 SQL 注入攻击。

### 事务管理

- `@Transactional` 注解仅用于 **Service 层**方法。
- 避免在循环中频繁提交事务，影响性能。

## 五、代码风格规范

### 命名规范

| 类型       | 命名方式             | 示例                  |
|------------|----------------------|-----------------------|
| 类名       | UpperCamelCase       | `UserServiceImpl`     |
| 方法/变量  | lowerCamelCase       | `saveUser()`          |
| 常量       | UPPER_SNAKE_CASE     | `MAX_LOGIN_ATTEMPTS`  |

### 注释规范

- 所有类、方法、字段需添加 **Javadoc** 注释。
- 注释采用中文编写以方便本地团队协作。

### 类型命名规范（阿里巴巴风格）

| 后缀 | 用途说明                     | 示例         |
|------|------------------------------|--------------|
| DTO  | 数据传输对象                 | `UserDTO`    |
| DO   | 数据库实体对象               | `UserDO`     |
| BO   | 业务逻辑封装对象             | `UserBO`     |
| VO   | 视图展示对象                 | `UserVO`     |
| Query| 查询参数封装对象             | `UserQuery`  |

### 实体类简化工具

- 使用 Lombok 注解替代手动编写 getter/setter/构造方法：
  - `@Data`
  - `@NoArgsConstructor`
  - `@AllArgsConstructor`

## 六、扩展性与日志规范

### 接口优先原则

- 所有业务逻辑通过接口定义（如 `UserService`），具体实现放在 `impl` 包中（如 `UserServiceImpl`）。

### 日志记录

- 使用 `@Slf4j` 注解代替 `System.out.println`

## 七、通用依赖规则总结

1. **数据库支持**
   - 支持 MySQL 和 Redis；
   - ORM 框架同时集成 JPA 与 MyBatis。

2. **认证授权机制**
   - 使用 JWT 进行 Token 验证和权限控制；
   - 定义了默认密钥及过期时间配置项。

3. **第三方服务集成**
   - 已引入阿里云 OSS SDK 及 HTTP Client 库；
   - 提供外部支付与物流查询接口模板。

4. **API 文档**
   - 集成 Swagger UI，便于快速查看和测试 RESTful API。

5. **单元测试**
   - 引入了 spring-boot-starter-test，适用于各类单元与集成测试场景。

## 八、编码原则总结

| 原则       | 说明                                       |
|------------|--------------------------------------------|
| **SOLID**  | 高内聚、低耦合，增强可维护性与可扩展性     |
| **DRY**    | 避免重复代码，提高复用性                   |
| **KISS**   | 保持代码简洁易懂                           |
| **YAGNI**  | 不实现当前不需要的功能                     |
| **OWASP**  | 防范常见安全漏洞，如 SQL 注入、XSS 等      |
