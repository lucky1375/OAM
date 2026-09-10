# 办公自动化管理系统（OAM）

面向企业内部办公场景的管理系统，工程骨架参考 PRODUCT（进销存管理系统）搭建。前端使用 Vue 2 构建单页应用，后端使用 Spring Boot 提供 REST API，数据存储采用嵌入式本地存储数据库（H2），无需额外安装数据库服务，开箱即用。

## 已实现功能（基础框架）

- **登录认证**：管理员/用户登录，基于 JWT 的无状态认证，密码 BCrypt 加密存储
- **账号管理**（管理员）：账号列表、新增、编辑、重置密码、启用/停用、删除；Excel 导出
- **权限管理**（管理员）：功能树（模块 + 操作按钮）授权，为用户勾选对应权限
- **菜单 / 按钮权限控制**：菜单与路由按权限码显隐，新增/编辑/删除/重置密码/导出等按钮按权限控制
- **数据审计字段**：列表展示创建人、创建时间、更新人、更新时间
- **默认管理员**：`admin / admin123`（空库首次启动自动创建）

> 业务模块（流程、报销、考勤、反馈、论坛等）为后续规划，尚未开发，可在现有 `common / auth / user` 基础框架上扩展。

## 功能规划

- **流程引擎（Activiti）**：集成 Activiti 工作流引擎，请假/报销/采购等审批流程建模与驱动，流程节点配置、待办/已办
- **报销管理**：报销单提交/审批，对接发票识别（OCR）自动提取票面信息并验真，减少手工录入
- **考勤打卡**：上下班打卡、考勤记录查询与统计、异常补卡申请
- **建议反馈**：意见/建议提交，分类汇总与处理状态跟踪
- **企业论坛**：内部社区发帖、评论、点赞，按版块组织
- **公告通知**：公告发布、阅读统计
- **日程会议**：日程安排、会议管理、会议室预定
- **任务协作**：任务分派、进度跟踪
- **组织架构**：部门、岗位、人员管理

## 技术栈

| 层 | 技术 | 版本 |
| --- | --- | --- |
| 前端 | Vue 2（Vue CLI + Vue Router + Vuex + Element UI + Axios） | 2.6.14 |
| 后端 | Spring Boot（Spring Web + Spring Data JPA + Validation） | 2.5.6 |
| 语言 | Java | 1.8 |
| 数据库 | 嵌入式本地存储数据库 H2（文件模式，无需安装） | 跟随 Spring Boot 管理 |
| 工作流 | Activiti 工作流引擎（规划中） | 7.x |
| 构建 | Maven / npm | - |

## 系统架构

前后端分离架构：

```
Vue 2 前端 (SPA)  --HTTP/REST-->  Spring Boot 后端  -->  H2 本地数据库(文件)
```

- 后端通过 Spring Data JPA 访问 H2，数据持久化到本地文件，重启不丢失
- 前端开发环境通过代理转发请求到后端，生产环境由后端托管静态资源或独立部署

## 目录结构

```
OAM
├── app/                        # 移动端客户端（预留）
│   ├── android/                #   Android 原生
│   ├── ios/                    #   iOS 原生
│   └── harmony/                #   鸿蒙（HarmonyOS / OpenHarmony）
├── frontend/                   # Vue 2 前端工程
│   ├── public/index.html
│   ├── vue.config.js           # devServer 端口 8082 与 /api 代理
│   └── src/
│       ├── api/
│       │   ├── request.js      # axios 封装（token 注入、统一错误处理）
│       │   └── modules/        # 按模块的接口封装（auth / user / permission）
│       ├── layout/             # 主布局（顶部导航、退出登录）
│       ├── views/
│       │   ├── auth/           # 登录页 Login
│       │   ├── home/           # 首页 Home
│       │   ├── user/           # 账号管理 UserManagement
│       │   └── permission/     # 权限管理 PermissionManagement
│       ├── router/             # 路由与登录/权限守卫
│       ├── store/
│       │   ├── index.js
│       │   └── modules/user.js # 认证状态管理（vuex module）
│       └── App.vue / main.js
└── src/main/java/com/oam/      # Spring Boot 后端工程（按模块分包）
    ├── OamApplication.java
    ├── common/                 # 通用基础模块
    │   ├── security/           #   JWT 工具、过滤器、Security 配置
    │   ├── exception/          #   全局异常处理
    │   ├── config/             #   DataInitializer 默认管理员初始化
    │   ├── permission/         #   PermissionCatalog 权限功能树
    │   ├── util/               #   ExcelUtil 导出工具
    │   └── dto/                #   ApiResponse 统一响应
    ├── auth/                   # 认证模块
    │   ├── controller/         #   AuthController（login / me）
    │   ├── service/            #   AuthService
    │   └── dto/                #   LoginRequest
    ├── user/                   # 账号管理模块
    │   ├── controller/         #   UserController
    │   ├── service/            #   UserService
    │   ├── repository/         #   UserRepository
    │   ├── entity/             #   User、Role、UserPermission
    │   ├── dto/                #   UserDto / Create / Update / ResetPassword
    ├── permission/             # 权限管理模块
    │   └── controller/         #   PermissionController
    └── resources/application.yml
```

## 快速开始

### 后端

```bash
mvn spring-boot:run
# 默认端口 8090；首次启动自动创建 H2 数据库与默认管理员 admin / admin123
```

H2 控制台（开发调试用）：`http://localhost:8090/h2-console`

### 前端

```bash
cd frontend
npm install
npm run serve
# 默认端口 8082，已配置代理转发 /api 到后端 8090
```

浏览器访问 `http://localhost:8082`，使用默认账号 `admin / admin123` 登录。

## 配置说明

关键配置项（`application.yml`）：

| 配置 | 说明 | 默认值 |
| --- | --- | --- |
| `server.port` | 后端服务端口 | `8090` |
| `spring.datasource.url` | H2 本地数据库文件路径 | `jdbc:h2:file:./data/oam-db` |
| `spring.jpa.hibernate.ddl-auto` | 表结构自动更新 | `update` |
| `spring.h2.console.enabled` | 是否开启 H2 控制台 | `true` |
| `app.jwt.secret` | JWT 签名密钥 | 内置（上线前务必修改） |
| `app.jwt.expiration` | JWT 有效期（毫秒） | `86400000`（24 小时） |

> 认证采用无状态 JWT：前端将 token 存入 `localStorage`，请求时通过 `Authorization: Bearer <token>` 携带；本工程为本地开发，CORS 已对任意来源开放。

## 主要接口

以 `/api` 为前缀：

| 模块 | 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- | --- |
| 认证 | POST | `/api/auth/login` | 登录，返回 token 与用户信息 | 公开 |
| 认证 | GET | `/api/auth/me` | 获取当前登录用户 | 已登录 |
| 账号 | GET | `/api/users` | 账号列表 | 管理员 |
| 账号 | POST | `/api/users` | 新增账号 | 管理员 |
| 账号 | GET | `/api/users/{id}` | 账号详情 | 管理员 |
| 账号 | PUT | `/api/users/{id}` | 编辑账号 | 管理员 |
| 账号 | PUT | `/api/users/{id}/password` | 重置密码 | 管理员 |
| 账号 | DELETE | `/api/users/{id}` | 删除账号（管理员账号不可删） | 管理员 |
| 权限 | GET | `/api/permissions/tree` | 功能树 | 管理员 |
| 权限 | GET | `/api/permissions/users/{id}` | 用户已授权权限 | 管理员 |
| 权限 | PUT | `/api/permissions/users/{id}` | 重新分配权限 | 管理员 |

## 部署

```bash
# 后端打包
mvn clean package -DskipTests
java -jar target/oam.jar

# 前端打包后由后端静态托管或 Nginx 部署
cd frontend && npm run build
```

> 注：登录后本地状态（token / 用户信息）保存在浏览器 `localStorage`，账号数据持久化在 H2 文件库。

## 变更记录

### 2026-09-10
- **工程初始化**：参考 PRODUCT 工程生成 OAM 基础文件
  - 后端 Spring Boot 骨架（`com.oam`）：common（security / exception / config / permission / util / dto）、auth、user、permission 模块
  - 前端 Vue 2 骨架：登录、首页、账号管理、权限管理，菜单与按钮权限控制
  - 项目级文件：`pom.xml`、`application.yml`、`.gitignore`、`README.md`、`app/` 移动端预留目录
