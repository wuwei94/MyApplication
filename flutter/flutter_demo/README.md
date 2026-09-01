# flutter_demo — Flutter Demo Catalog

一个面向学习、验证和沉淀示例的 Flutter Demo Catalog 项目。

它不是面向业务场景的完整 App，而是一个可以持续扩展的示例目录：通过首页浏览不同 Demo 分组，进入目录页查看具体示例，再进入最终页面验证组件、框架或能力的使用方式。同时，这个仓库也是一个 Flutter module，可用于 add-to-app 集成。

## 项目特点

- **Demo Catalog First 架构**：`app` 负责壳层和路由，`catalog` 负责目录树和解析，`demos` 负责示例内容
- **双路由支持**：同时支持 `GoRouter` 和 `AutoRoute`，通过常量切换路由实现
- **14 个顶层分组**：basics / layout / state_management / network / bluetooth / storage / image / animation / engine / video / ml / chart / packages / showcase
- **100+ 个叶子示例页面**：覆盖 Flutter 与端侧现代开发绝大部分核心与深度场景
- **add-to-app 集成**：保留 Flutter module 能力，可用于原生宿主 App 的集成

## 目录结构

```
lib/
├── main.dart                          # 应用入口
├── app/                               # App 壳层、首页、导航与路由配置
├── catalog/                           # Catalog 模型、目录页、注册表、路由工厂、路径解析
├── demos/                             # 所有示例内容（14 个分组）
├── core/                              # 通用工具能力：网络、日志、存储、UI 辅助等
├── l10n/                              # 多语言与字符串资源
└── boost/                             # add-to-app / Boost 相关桥接代码
```

## 文档目录

| 文档 | 内容 |
|------|------|
| docs/demos.md | 示例分组详情（14 个分组，130+ 个叶子页面） |
| docs/dependencies.md | 依赖清单（按分类列出所有三方包） |
| docs/conventions.md | 开发约定（Catalog 设计、路由模式、编码规范） |

## 网络封装

本仓库通过 `../flutter_libs/` 下的两个独立本地 package 提供 Dio 和 package:http 两套客户端，便于对比底层能力和调用方式：

- `DioClient`：GET/POST/PUT/PATCH/DELETE、form/json/raw 请求体、业务 `data` decoder 和 `CancelToken`
- `HttpClient`：GET/POST/PUT/PATCH/DELETE、form/json/raw 请求体、业务 `data` decoder 和基于 `AbortableRequest` 的真实取消
- 两个 package 的 `NetworkResponse<T>` 统一使用 `code/message/data`，`isSuccess` 与 Android 一样表示业务码为 `0`
- 标准业务信封按 JSON 响应解析，JSON 字段为 `errorCode/errorMsg/data`，`errorCode` 按整数类型读取
- `NetworkException` 只保留 `code/message/cause`，HTTP 状态码与 `1000–1004` 网络错误码与 Android `ApiException` 对齐
- 非零业务码默认交给调用方检查；调用 `requireSuccess()` 会通过 `ServerResultException` 进入统一异常链
- 两个 package 的日志行为沿用各自实现，不纳入普通请求契约，也不额外脱敏；package:http 启用日志后原样输出 Header/Body
- 业务信封、decoder 或类型转换失败统一映射为解析错误码 `1004`
- 重定向、代理、Cookie 和认证等传输策略交给 Dio/package:http 或调用方注入的 Client，不在包装层重复实现
- 注入 Dio 时通过 clone 隔离 BaseOptions，不会修改共享 Dio；底层 adapter 和 interceptor 按 Dio clone 契约共享
- 包装器只关闭自己创建的底层 Client；注入实例始终由调用方管理，包装器关闭后不可复用
- package:http 使用 `Uri` 规则解析 base URL、相对路径和绝对 URL

完整能力对齐表见仓库根目录 `docs/libs.md`。

## 快速开始

### 环境要求

- Flutter：项目通过 `.fvmrc` 固定为 `3.47.0`
- Dart SDK：`3.13.0`（随 Flutter SDK 提供）
- 推荐使用 `fvm`

### 安装依赖

```bash
fvm flutter pub get
```

### 运行项目

```bash
fvm flutter run
```

### 常用命令

```bash
fvm flutter analyze    # 静态检查
fvm flutter test       # 运行测试
dart format lib test tools  # 代码格式化
```

## 不变量

1. 新增示例必须在对应 `catalog.dart` 中注册
2. 叶子页面统一使用 `xxx_demo.dart` 命名
3. 必须同时支持 GoRouter 和 AutoRoute
4. 遵守 `analysis_options.yaml` 编码规范
5. 修改代码时必须同步更新文档，详见 `docs/conventions.md`
