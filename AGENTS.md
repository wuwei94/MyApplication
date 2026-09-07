# AGENTS.md

> 本文件是 Agent 知识的目录索引，而非百科全书。
> 控制在 100 行以内，指向更深层的文档来源。

## 项目概述

MyApplication 是一个个人 Android 技术栈沉淀项目，用于展示 Android 开发模式、库和最佳实践。采用 Kotlin、多模块架构和 ARouter 路由导航。

**技术栈**：Kotlin、Jetpack Compose、ARouter、Hilt / Koin、Coroutines、Flow、CameraX、Flutter

## 架构

```
app/                          # 入口（SplashScreen → DirectoryActivity）
basic/                        # 基础层
├── basic_lib/                # 基类（BaseActivity、BaseVBActivity、权限管理）
├── basic_shared/             # 路由（RouterPath）、共享 Layout、Utils
├── basic_model/              # 纯领域模型层（JVM 库，零 Android 依赖，领域实体与版本游标）
├── basic_datastore/          # 键值与偏好存储（Tencent MMKV、游标持久化与 StateFlow 响应式流）
├── basic_database/           # 本地持久化数据库层（Room 实体、DAO、Database 实例）
├── basic_network/            # 远程网络数据通信层（Retrofit API、网络 DTO、远程数据源）
├── basic_repo/               # 数据仓库层（纯数据仓库聚合门面、SSOT、离线拉取契约）
└── basic_sync/               # 增量同步调度层（WorkManager 离线增量调度、Sync 门面）
flutter/                      # Flutter 层
├── flutter_demo/             # Flutter Demo Catalog 子工程（Flutter module，add-to-app 集成）
└── flutter_libs/             # Flutter 本地库（lib_network_dio / lib_network_http / lib_image_loader / lib_event_bus / lib_storage / lib_bluetooth 独立封装）
libs/                         # 库封装层（无 Activity，仅提供 API 封装）
modules/                      # 功能模块层（每个模块有独立入口 Activity）
build-logic/                  # 构建逻辑层（Convention Plugin + 依赖配置）
└── convention/               # → 详见 docs/build-logic.md
```

## 文档目录

> 完整四维导览与技术全景详见 [docs/README.md](docs/README.md)。

| 分类 | 文档 | 内容 |
|------|------|------|
| **工程基建** | docs/conventions.md | 关键约定（路由、模块结构、示例页面、Activity 基类、构建命令） |
| | docs/comments.md | 代码注释规范（语言、KDoc 格式、内容层次、保护准则） |
| | docs/design.md | 设计规范（间距、文字、圆角、图标尺寸体系） |
| | docs/git.md | Git 提交规范（Conventional Commits、提交模板、钩子门禁与历史遗留） |
| | docs/build-logic.md | 构建逻辑（22 个 Convention Plugin 配置详情） |
| | docs/engineering.md | 现代 Android 工程化实践指南（构建系统、静态治理、测试三支柱、性能基准、门禁防御） |
| **架构评估** | docs/architecture.md | 架构模式演进与选型指南（MVP / MVVM / MVI / Compose MVI / Mavericks / Offline-First） |
| | docs/modularization.md | 现代组件化架构演进与技术选型（ARouter 停更原因、大厂主流方案、API-Impl 与 TheRouter） |
| | docs/nia-adoption.md | Now in Android 落地评估（能力落差对照、12 项落地方案与推进顺序、不建议照搬清单） |
| | docs/testing.md | 测试体系（Turbine + 手写测试替身、测试命名 Lint 规则、Roborazzi 截图测试） |
| **专项指南** | docs/network.md | OkHttp、Retrofit、Retrofit Rx 与 Ktor 的使用约定和功能边界 |
| | docs/transfer.md | Rx 文件上传、下载、断点续传与并发队列约定 |
| | docs/bluetooth.md | 低功耗蓝牙（BLE 客户端）开发指南、8 大核心功能与三方库对比 |
| | docs/di.md | 依赖注入方案对比（Hilt vs Koin 原理、语法、作用域与选型） |
| | docs/event.md | 事件总线方案对比（EventBus / RxEventBus / LiveEventBus / FlowEventBus 特性与选型） |
| | docs/ml.md | 端侧机器学习开发指南（LiteRT / TensorFlow Lite 图像分类与生成） |
| | docs/performance.md | 性能优化实践指南（基准测试、掉帧治理、AOT 与优化矩阵） |
| **仓内清单** | docs/modules.md | 功能模块详情（30 个模块，每个模块的 Activity 列表） |
| | docs/libs.md | 库封装层职责索引 |

## 不变量

1. 每个 Activity 必须有 `@Route` 注解
2. 每个模块必须依赖 `basic_lib` 和 `basic_shared`
3. 新模块必须在 `settings.gradle.kts` 中注册
4. 资源文件必须使用模块前缀（`<模块名>_`）
5. 每个 Activity 都应有实际内容，不能是空壳或纯模板代码
6. 示例页面优先直接展示库 API，不在页面实现与示例目标无关的任务编排器，详见 `docs/conventions.md`
7. 修改代码时必须同步更新 `README.md` 和相关文档，详见 `docs/conventions.md`
8. 代码注释与文档默认使用中文（标识符、类型名与必要的专有术语除外）
9. 默认不为极少数、违约或纯理论输入增加校验、分支、异常类型或公共 API，详见 `docs/conventions.md`
10. 问题治理必须深入底层机理从根源解决，严禁使用补丁式、兜底式手段掩盖问题，详见 `docs/conventions.md`
11. 测试类名以 `Test` 结尾、测试方法名为 `被测对象_场景_预期结果` 下划线式（由 `lint` 模块规则强制），详见 `docs/testing.md`
12. 修改已有代码时严禁覆盖、裁剪或弱化既有的技术选型、方案对比、设计约束等 KDoc 注释；代码修改必须采用最小范围切片，未变更的注释块严禁划入替换范围，详见 `docs/comments.md`

