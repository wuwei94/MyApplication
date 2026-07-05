# 📱 MyApplication — Android 个人技术栈沉淀工程


一个用于沉淀 Android 技术栈、源码阅读与跨端实践的个人学习工程。工程上采用 **Kotlin DSL + Version Catalogs + Convention Plugin**（参考 [Now in Android](https://github.com/android/nowinandroid) 结构）进行统一版本管理和组件化拆分；能力层覆盖 **MVP / MVVM / MVI / Mavericks**，并接入 **Jetpack Compose / Flutter** 双栈视图层，方便横向对比不同架构与 UI 方案。

---

## ✨ Highlights

- **工程化**：Kotlin DSL + Version Catalogs + `build-logic` Convention Plugin，ARouter 模块通信，Hilt 多模块初始化。
- **架构层**：MVP / MVVM / MVI / Mavericks 全覆盖，配套 `UseCase` + `Repository` + `ServiceLocator` 脚手架。
- **网络层**：Volley / OkHttp / Retrofit / Ktor / WebSocket / Netty / NanoHTTPD，含拦截器、下载库、点九图解析。
- **持久层**：Room / GreenDAO / ObjectBox 三种 ORM 对比 + DataStore（Preferences / Proto）。
- **消息总线**：RxBus / LiveDataBus / FlowBus 三种方案对比实现。
- **跨端**：Android Native + Flutter 双栈落地，Flutter 内覆盖 Dio / Provider / GetX / BloC。
- **Coroutines + Flow**：配合 `repeatOnLifecycle`、`DataStore`、`Paging`、`WorkManager` 等 Jetpack 组件实践。
- **自定义 View**：高斯模糊、裸眼 3D、跑马灯、无限滚动 ImageView、验证码控件等。
- **Compose**：Navigation、BackHandler、手势 / 拖拽 / `rememberSaveable` 等原生能力示例。

---

## 🧱 Tech Stack

| Layer       | Tech |
|-------------|------|
| Language    | Kotlin |
| Build       | Gradle Kotlin DSL · Version Catalogs · Convention Plugin |
| UI          | Android Views · Jetpack Compose · Material3 · Flutter |
| Architecture| MVP · MVVM · MVI · Mavericks |
| DI          | Hilt |
| Navigation  | ARouter · Navigation Component |
| Network     | OkHttp · Retrofit · Ktor · Volley · WebSocket · Netty · NanoHTTPD |
| Persistence | Room · GreenDAO · ObjectBox · DataStore |
| Image       | Glide · Coil |
| Reactive    | Coroutines · Flow · RxJava 3 · LiveData |
| Messaging   | EventBus · RxBus · LiveDataBus · FlowBus |
| Others      | WorkManager · Paging 3 · SplashScreen · MMKV |
| Quality     | Dependency Guard（已接入） |

> 各库版本详见 `gradle/libs.versions.toml`。

---

## 📁 Project Structure

```
MyApplication/
├── app                         # 壳工程（Hilt + ARouter 入口）
├── build-logic                 # Convention Plugin，统一插件配置
├── gradle/libs.versions.toml   # 统一版本目录
├── basic                       # 基础设施层
│   ├── basic_lib               # BaseActivity / Fragment / ViewModel / 通用工具
│   ├── basic_module            # 通用 Bus、Router、UI 脚手架
│   ├── basic_data              # 通用数据源 / OkHttp / Retrofit 基础封装
│   └── basic_repo              # Repository 基类、Room、依赖装配
├── libs                        # 可复用的业务能力库
│   ├── lib_okhttp / lib_retrofit / lib_ktor / lib_volley / lib_websocket / lib_download
│   └── lib_eventbus / lib_ninepatch / lib_imageloader / lib_widget
└── modules                     # Feature 模块
    ├── module_arch              # 架构 Demo（MVP / MVVM / MVI / Mavericks）
    ├── module_network           # 网络 Demo
    ├── module_sample            # Jetpack 组件 Demo
    ├── module_opensource        # 开源 Demo
    ├── module_compose           # Compose 示例
    ├── module_widget / module_utils / module_database / module_demo / module_libraries
    └── module_flutter           # Flutter 子工程
```

---

## 📚 Libs Overview

### 自定义控件

* 高斯模糊
* BottomSheetDialog
* 无限滚动的ImageView
* 跑马灯控件
* 裸眼3D效果
* 自动对齐TextView
* 自定义PageTransformer
* 验证码控件

### 网络 / 框架

* Volley封装
* OkHttp封装
* Retrofit封装
* Retrofit下载库
* RxWebSocket封装
* 点九图片解析库
* RxBus + LiveDataBus + FlowBus

| Bus          | Delayed | Ordered | Sticky | Lifecycle | Cross-process | Thread dispatch |
|--------------|---------|---------|--------|-----------|---------------|-----------------|
| EventBus     | ❌      | ✅      | ✅     | ❌        | ❌            | ✅              |
| RxBus        | ❌      | ✅      | ✅     | ❌        | ❌            | ✅              |
| LiveEventBus | ✅      | ✅      | ✅     | ✅        | ✅            | ❌              |
| FlowEventBus | ✅      | ✅      | ✅     | ✅        | ❌            | ✅              |

---

## 📦 Modules Detail

### module_arch

* MVP
* MVVM
* MVI
* 基于 mavericks 框架的 MVI

#### MVVM

* LiveData + ViewModel
* 使用 UseCase 封装可复用的单一业务逻辑

#### MVI

* 定义状态 State
* 定义事件 Event
* 处理事件 + 更新状态（ViewModel）
* 处理状态 + 发送事件（UI）

### module_network

* Coil
* Glide
* HttpURL
* Volley
* OkHttp
* Retrofit
* WebSocket网络请求
* 基于OkHttp实现WebSocket
* NanoHttpD的Android端的服务器搭建
* WebServer的Android端的服务器搭建
* 基于Netty实现的socket

### module_opensource

* PAG
* Lottie
* SVGAPlayer
* Greendao 数据库
* ObjectBox 数据库

### module_sample

* Hilt
* Room
* Paging
* DataStore
* WorkManager
* Android 上的 Kotlin 协程
* Android 上的 Kotlin 数据流

### module_compose

* 基础组件
* Navigation
* BackHandler
* remember, rememberSaveable
* draggable, dragGestureDetector

### module_flutter

* 布局类组件
    * Row / Column · Flex · Wrap / Flow · Stack
* 容器类组件
    * Container · Padding · Align · Center · ConstrainedBox · DecoratedBox · SizedBox
* 可滚动组件
    * ListView · GridView · SingleChildScrollView · PageView · TabBarView · AnimatedList · CustomScrollView · NestedScrollView
* 功能型组件
    * LayoutBuilder · GestureDetector · PopScope · InheritedWidget · ValueListenableBuilder · FutureBuilder / StreamBuilder
* 其他组件
    * Animation · Dialog · Isolate
* 网络请求
    * [Dio](https://pub.dev/packages/dio)
* 状态管理
    * [Provider](https://pub.dev/packages/provider) · [GetX](https://pub.dev/packages/get) · [BloC](https://pub.dev/packages/flutter_bloc)
* 三方框架
    * [Toast](https://pub.dev/packages/fluttertoast) · [Notification](https://pub.dev/packages/flutter_local_notifications) · [SharedPreferences](https://pub.dev/packages/shared_preferences) · [ScreenUtil](https://pub.dev/packages/flutter_screenutil)

---

## 📖 源码笔记

我把常见组件与开源框架的源码阅读笔记沉淀在个人知识库（Flowus），与本工程配套的核心笔记：

### Android 基础

* [so库适配简单总结](https://flowus.cn/williamwu/share/6f18d2cd-9df9-4637-bdea-1d4e89919876)
* [RecyclerView 绘制流程](https://flowus.cn/williamwu/share/a20dab7f-b70c-4272-9353-b60dd832c7b2)
* [RecyclerView 缓存机制](https://flowus.cn/williamwu/share/36be3fd1-6c8d-4164-bd9a-bd8df49e7557)

### Jetpack 源码

* [Lifecycle 源码解析](https://flowus.cn/williamwu/share/fab8b772-2374-4687-ac80-746ec914dda8)
* [LiveData 源码解析](https://flowus.cn/williamwu/share/6e97c045-679e-4f16-93bf-3f5b0b35d8b4)
* [ViewModel 源码解析](https://flowus.cn/williamwu/share/a8af4987-102c-436d-89a9-4a42a483c019)

### 开源框架源码

* [OkHttp 源码解析](https://flowus.cn/williamwu/share/1fb573d6-a924-4441-a20d-2f7de2f9d195)
* [Retrofit 源码解析](https://flowus.cn/williamwu/share/c8e3e04c-d24b-49af-847d-4a8be6646e2e)
* [Glide 执行流程](https://flowus.cn/williamwu/share/fd31dd11-4bcb-45d9-8f94-772a8628b69e)
* [Glide 缓存机制](https://flowus.cn/williamwu/share/2ce6971d-2566-4fe6-ab5a-d24e98fffe80)
* [ARouter 源码解析](https://flowus.cn/williamwu/share/bd1b9ab3-c881-42aa-a209-07336402660e)
* [EventBus 源码解析](https://flowus.cn/williamwu/share/55d699dd-2728-4e68-b2c8-b3d3db9d89a1)
* [LeakCanary 源码解析](https://flowus.cn/williamwu/share/3dab5ccc-2d12-45f7-83a4-828ce87504e)

---

## 📜 License

个人学习项目，欢迎参考 / fork / 提 Issue。
