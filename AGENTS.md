# AGENTS.md

> 本文件是 Agent 知识的目录索引，而非百科全书。
> 控制在 100 行以内，指向更深层的文档来源。

## 项目概述

MyApplication 是一个个人 Android 技术栈沉淀项目，用于展示 Android 开发模式、库和最佳实践。采用 Kotlin、多模块架构和 ARouter 路由导航。

**技术栈**：Kotlin、Jetpack Compose、ARouter、Hilt、Coroutines、Flow、CameraX、Flutter

## 架构

```
app/                          # 入口（SplashScreen → ModuleActivity）
basic/                        # 基础层
├── basic_lib/                # 基类（BaseActivity、BaseVBActivity、权限管理）
├── basic_shared/             # 路由（RouterPath）、共享 Layout、Utils
└── basic_repo/               # 数据仓库层
flutter/                      # Flutter 层
├── flutter_demo/             # Flutter Demo Catalog 子工程（Flutter module，add-to-app 集成）
└── flutter_libs/             # Flutter 本地库（lib_network_dio / lib_network_http / lib_image_loader / lib_event_bus / lib_storage 独立封装）
libs/                         # 库封装层（无 Activity，仅提供 API 封装）
modules/                      # 功能模块层（每个模块有独立入口 Activity）
build-logic/                  # 构建逻辑层（Convention Plugin + 依赖配置）
└── convention/               # → 详见 docs/build-logic.md
```

## 文档目录

| 文档 | 内容 |
|------|------|
| docs/modules.md | 功能模块详情（21 个模块，每个模块的 Activity 列表） |
| docs/libs.md | 库封装层职责索引 |
| docs/network.md | OkHttp、Retrofit、Retrofit Rx 与 Ktor 的使用约定和功能边界 |
| docs/transfer.md | Rx 文件上传、下载、断点续传与并发队列约定 |
| docs/build-logic.md | 构建逻辑（22 个 Convention Plugin 配置详情） |
| docs/conventions.md | 关键约定（路由、模块结构、示例页面、Activity 基类、构建命令） |
| docs/design.md | 设计规范（间距、文字、圆角、图标尺寸体系） |
| docs/comments.md | 代码注释规范（语言、KDoc 格式、内容层次） |

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
