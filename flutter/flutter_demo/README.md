# flutter_demo — Flutter Demo Catalog

一个面向学习、验证和沉淀示例的 Flutter Demo Catalog 项目，同时是 `MyApplication` 仓 Flutter 双栈的落地载体。

它不是面向业务场景的完整 App，而是一个可以持续扩展的示例目录：通过首页浏览不同 Demo 分组，进入目录页查看具体示例，再进入最终页面验证组件、框架或能力的使用方式。同时它也是一个 Flutter module，可被 Android 宿主以 add-to-app 方式嵌入。

## 项目特点

- **Demo Catalog First 架构**：`app` 负责壳层与路由，`catalog` 负责目录模型、注册表、解析与路由工厂，`demos` 负责示例内容——新增示例只需要加页面 + 在 `catalog.dart` 注册
- **15 个顶层分组、148 个叶子示例页**：basics / layout / state_management / network / bluetooth / storage / image / animation / engine / video / ml / chart / packages / markdown / showcase
- **双路由支持**：`GoRouter` 与 `AutoRoute` 同时实现，在 `lib/app/router/app_router_type.dart` 用 `AppRouterType` 常量切换，路由表互不影响
- **双端主题对照**：与仓内 Android Demo 按主题一一对应（见下方对照表），用于对比同一能力在两端的技术选型差异
- **add-to-app 集成**：作为 Flutter module 被 Android `:modules:module_flutter` 嵌入，宿主侧用 `-PenableFlutter=false` 可整体摘除 Flutter 依赖

## 目录结构

```
lib/
├── main.dart                          # 应用入口
├── app/                               # 壳层、首页、导航与路由配置（app/router 双路由、app/navigation）
├── catalog/                           # Catalog 模型 / 目录页 / 注册表 / 路由工厂 / 目录树解析
│   ├── models/                        # CatalogEntry / CatalogSection / ResolvedCatalogEntry
│   ├── pages/                         # 目录页
│   ├── registry/                      # catalog_registry（顶层分组注册）
│   ├── routing/                       # catalog_route_factory（目录树 → 路由）
│   └── services/                      # catalog_tree_resolver
├── demos/                             # 15 个分组示例，每个分组用 catalog.dart 描述目录结构
├── core/                              # 通用能力：basic / constants / utils / widgets（日志、对话框、内联控制台等）
├── l10n/                              # 多语言字符串资源
├── objectbox/                         # ObjectBox 生成代码（objectbox.g.dart / model）
└── boost/                             # add-to-app / Boost 桥接

../flutter_libs/                       # 8 个可复用本地 package（见「本地库」）
docs/                                  # demos / dependencies / conventions
tools/                                 # android（6 个补丁脚本）/ ios（2 个）与 dart-define 同步
test/                                  # asr_engine_test（46）/ ml_zip_npy_test（14）/ widget_test
```

## 示例分组

| 分组 | 内容 | 示例数 | Android 对照 |
|------|------|--------|--------------|
| basics | 基础示例与完整小型应用（counter、GetX App） | 2 | module_widget |
| layout | 布局与交互，14 个子目录（容器 / 线性 / 层叠 / 流式 / Sliver / 滚动 / 弹窗 / 手势 / 过渡 / 自适应 / 异步 / 装饰 / 状态原语） | 14 个子目录 | module_widget、module_tab |
| state_management | Provider / Cubit / Bloc / Riverpod | 4 | module_arch |
| network | Dio / package:http / WebSocket / MQTT | 4 | module_http、module_socket、module_mqtt |
| bluetooth | 扫描 / GATT 读写与 Notify / MTU 协商与分包 | 3 | module_bluetooth |
| storage | 键值（shared_preferences / secure_storage / Hive）、关系型（Drift / Sqflite）、对象库（Isar / ObjectBox）、门面与路径工具 | 9 | module_storage、module_database |
| image | 加载与缓存、压缩、裁剪、选择、缩放 | 9 | module_image_loader |
| animation | Lottie / PAG / SVG / SVGA | 4 | module_anim |
| engine | CustomPainter 粒子、签名板、自定义 RenderObject 环形布局、交错动画、GLSL 着色器、沿路径动画、3D 翻转、自定义手势识别 | 8 | 无对应（Flutter 引擎层专有） |
| video | video_player / Chewie | 2 | module_media |
| ml | LiteRT 端侧推理 9 个任务（视觉 3 / 文本 2 / 语音 4） | 9 | module_ml |
| chart | fl_chart 折线 / 柱状 / 饼图 / 雷达图 / 多图表联动看板 | 5 | module_chart |
| markdown | 渲染 / 多语言代码高亮 / 流式打字机 / AI 流式对话 | 4 | module_markdown |
| packages | 三方包：字体、链接识别、头像、通知、权限、WebView、屏幕适配、下拉刷新 | 大量 | module_widget_thirdparty |
| showcase | 杂项展示（本地字体等） | 少量 | module_sample |

每个分组的页面清单与能力说明见 [docs/demos.md](docs/demos.md)。

## 与 Android 侧的关系

「对应」指**功能对应**，不是架构照搬：

| 维度 | Android（`modules/`） | 本子工程 |
|------|----------------------|----------|
| 页面组织 | View/XML 多 Activity + ARouter | Catalog 目录树 + `GoRouter` / `AutoRoute` 双路由 |
| 导航配置 | `@Route(path = RouterPath.Xxx.Yyy)` 注解 | `catalog.dart` 声明 + `catalog_registry` 注册 |
| ML 运行时 | LiteRT 2.x `CompiledModel`（语音合成两张 variable-tensor 图走 `Interpreter`） | `tflite_flutter` 经典 `Interpreter` + 多签名图 FFI（见下） |
| 状态管理 | LiveData / Flow / ViewModel | Provider / Cubit / Bloc / Riverpod |

## 本地库

可复用的 Flutter 封装都在上一级 `../flutter_libs/`，各自独立成 package，公共 API 从包根导出、不反向依赖 `flutter_demo`：

| 包 | 职责 | 依赖 |
|----|------|------|
| `lib_network_dio` | Dio 请求封装（业务 decoder、`CancelToken` 取消） | Dio |
| `lib_network_http` | `package:http` 请求封装（业务 decoder、`AbortableRequest` 取消） | package:http |
| `lib_image_loader` | 图片加载门面（`IImageLoader` 接口 + 内核可切换，默认 cached_network_image） | cached_network_image |
| `lib_storage` | 键值存储封装（Hive / shared_preferences 内核可切换） | Hive、shared_preferences |
| `lib_event_bus` | 事件总线封装 | event_bus |
| `lib_bluetooth` | BLE 通信封装（基于 flutter_blue_plus 的面向对象抽象） | flutter_blue_plus |
| `lib_mqtt` | MQTT 客户端封装 | mqtt_client |
| `lib_websocket` | WebSocket 客户端封装 | web_socket_channel |

### 网络契约（Dio / http ↔ Retrofit）

- 两个 package 的 `NetworkResponse<T>` 统一使用 `code/message/data`，`isSuccess` 与 Android 一样表示业务码为 `0`
- 标准业务信封按 JSON 响应解析，JSON 字段为 `errorCode/errorMsg/data`，`errorCode` 按整数类型读取
- `NetworkException` 只保留 `code/message/cause`，HTTP 状态码与 `1000–1004` 网络错误码与 Android `ApiException` 对齐
- 非零业务码默认交给调用方检查；调用 `requireSuccess()` 会通过 `ServerResultException` 进入统一异常链
- 业务信封、decoder 或类型转换失败统一映射为解析错误码 `1004`
- 重定向、代理、Cookie、认证、重试由 Dio `Interceptor`/adapter 或注入的 `http.Client` 承担，包装层不重复实现、不持有业务 Token
- 注入 Dio 时通过 clone 隔离 `BaseOptions`，包装器只关闭自己创建的底层 Client
- 日志沿用各自实现，不纳入请求契约也不脱敏（package:http 启用日志后原样输出 Header/Body）

完整的 Flutter 与 Retrofit 能力对齐表见 [`../../docs/05-catalog/libs.md`](../../docs/05-catalog/libs.md)。

## 文档目录

| 文档 | 内容 |
|------|------|
| [docs/demos.md](docs/demos.md) | 15 个分组的页面清单与能力说明 |
| [docs/dependencies.md](docs/dependencies.md) | 依赖清单（含 `tflite_flutter` 运行时升级说明） |
| [docs/conventions.md](docs/conventions.md) | 开发约定（Catalog 设计、路由模式、编码规范、文档同步） |
| [AGENTS.md](AGENTS.md) | 本子仓的硬性契约：项目定位、架构、文档同步、完成门禁 |
| [SKILLS.md](SKILLS.md) | 页面新增与开发任务的自定义 Skill 指引 |

## 快速开始

### 环境要求

- Flutter：项目通过 `.fvmrc` 固定为 `3.47.0`
- Dart SDK：随 Flutter SDK 提供（`pubspec.yaml` 约束 `^3.10.8`）
- 推荐使用 `fvm`

### 安装与运行

```bash
fvm flutter pub get
fvm flutter run
```

### 常用命令

```bash
fvm flutter analyze                   # 静态检查（硬要求：零 warning / 零 error）
fvm flutter test                      # 运行测试
dart format lib test tools            # 代码格式化
```

### 测试说明

- `test/asr_engine_test.dart`（46 用例）与 `test/ml_zip_npy_test.dart`（14 用例）是纯 Dart 用例，可直接 `dart test test/asr_engine_test.dart`
- `test/widget_test.dart` 依赖 Flutter 绑定，必须走 `fvm flutter test`
- `lib/demos/ml/engines/asr/` 下的 tokenizer / audio source / merger 保持零原生依赖（不 import `ml_signature.dart`），`asr_engine_test` 是这条边界的守门人

## 平台补丁与端侧模型

`tools/` 下的脚本用于把上游插件与工程配置补到可用状态，改动插件或升级依赖后需要重跑：

| 脚本 | 作用 |
|------|------|
| `tools/android/apply_android_tflite_flutter.dart` | 把 `tflite_flutter` 依赖的 LiteRT 运行时从 1.4.0 升到 **2.3.0**（`libLiteRt.so`，与 Android 侧同版本），并把 `litert-gpu` 钉回 1.4.0 以保留 `GpuDelegateV2` |
| `tools/android/apply_android_compile_sdk.dart`、`apply_android_flutter_compile_sdk.dart` | 统一 compileSdk |
| `tools/android/apply_android_permissions.dart` | 补齐蓝牙 / 相机等运行时权限声明 |
| `tools/android/apply_android_camera_camerax.dart`、`apply_android_desugaring.dart` | CameraX 与 desugaring 配置 |
| `tools/ios/apply_ios_geolocator_*.dart` | iOS geolocator 的 Info.plist / Podfile 配置 |
| `tools/apply_android_fixes.dart`、`apply_ios_fixes.dart`、`sync_dart_defines.dart` | 批量入口与 `dart_defines` 同步 |

ML 分组的模型资产**不入库**，与官方 sample 一致在首次使用时运行时下载（带断点续传与「已下载不重下」），最小约 32 MB、最大约 1.67 GB；只有轻量模型随 APK 打包。

## add-to-app 集成

Android 宿主为 `:modules:module_flutter`（`FlutterMainActivity`，路由 `/Flutter`）：

- `enableFlutter=true`（默认）：模块切到 `src/flutter` 源集并依赖 `:flutter` 工程
- `-PenableFlutter=false`：切到 `src/noFlutter` 源集、不依赖 Flutter，用于无 Flutter SDK 的环境与 CI
- 嵌入桥接代码在 `lib/boost/boost_navigator.dart`

## 不变量

1. 新增示例必须在对应 `catalog.dart` 中注册；只有新增顶层分组时才动 `lib/catalog/registry/catalog_registry.dart`
2. 叶子页面统一使用 `xxx_demo.dart` 命名，优先放进已有分类目录
3. 必须同时支持 GoRouter 和 AutoRoute，不得移除 `AppRouterType` 双路由切换能力
4. 遵守 `analysis_options.yaml` 编码规范，`fvm flutter analyze` 保持零 warning / 零 error
5. 修改代码时必须同步更新文档，详见 [docs/conventions.md](docs/conventions.md)
