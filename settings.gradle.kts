// 类型安全的项目访问器：dependencies 中的字符串项目路径（如 :basic:basic_lib）改为
// projects.basic.basicLib 访问器形式，由 Gradle 在编译构建脚本时校验项目路径，
// 拼错或模块被移除将直接编译失败，而不是等到运行期才暴露。
// 注意：该访问器只能用于依赖声明，动态路径仍需使用 project(path)，见 AndroidDeps.kt。
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/google")
            name = "Aliyun Google"
            content {
                excludeGroupByRegex("com\\.google\\.firebase.*")
            }
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
            name = "Aliyun Central"
        }
        maven {
            url = uri("https://jitpack.io")
            name = "JitPack"
        }
        maven {
            url = uri("https://tencent-tds-maven.pkg.coding.net/repository/shiply/repo")
            name = "Shiply"
        }
        maven {
            url = uri("https://storage.flutter-io.cn/download.flutter.io")
            name = "Flutter"
        }
    }
}
dependencyResolutionManagement {
    /**
     * PREFER_PROJECT(true)--首选项目存储库
     * PREFER_SETTINGS(false)--首选设置存储库
     * FAIL_ON_PROJECT_REPOS(false)--强制设置存储库
     */
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.aliyun.com/repository/google")
            name = "Aliyun Google"
            content {
                excludeGroupByRegex("com\\.google\\.firebase.*")
            }
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
            name = "Aliyun Central"
        }
        maven {
            url = uri("https://jitpack.io")
            name = "JitPack"
        }
        maven {
            url = uri("https://tencent-tds-maven.pkg.coding.net/repository/shiply/repo")
            name = "Shiply"
        }
        maven {
            url = uri("https://storage.flutter-io.cn/download.flutter.io")
            name = "Flutter"
        }
    }
}
// 名称需匹配 [a-zA-Z]([A-Za-z0-9\-_])*，否则类型安全项目访问器无法生成（含空格的名称会直接构建失败）。
// 该名称只用于 Gradle 工程标识，应用展示名由 app/src/main/res/values/strings.xml 的 app_name 决定。
rootProject.name = "MyApplication"

// 壳工程入口
include(":app")

// 基础层
include(":basic:basic_lib")
include(":basic:basic_shared")

// 数据仓库层
include(":basic:basic_repo")
include(":basic:basic_server")

// HTTP 客户端（基础）
include(":libs:lib_httpurl")
include(":libs:lib_volley")

// HTTP 客户端（封装层）
include(":libs:lib_okhttp")
include(":libs:lib_retrofit")
include(":libs:lib_retrofit_rx")

// HTTP 客户端（Kotlin 原生）
include(":libs:lib_ktor")

// RxJava 动态请求 / 文件传输
include(":libs:lib_rx_request")
include(":libs:lib_rx_download")
include(":libs:lib_rx_upload")

// 长连接 / WebSocket
include(":libs:lib_websocket_okhttp")
include(":libs:lib_websocket_java")
include(":libs:lib_netty")

// 流式推送 / SSE
include(":libs:lib_sse_okhttp")
include(":libs:lib_sse_ktor")

// MQTT 消息队列遥测传输
include(":libs:lib_mqtt")
include(":libs:lib_mqtt_hivemq")
include(":libs:lib_mqtt_paho_service")

// 服务端
include(":libs:lib_nanohttpd")

// 图片加载
include(":libs:lib_image_loader")

// 消息总线
include(":libs:lib_eventbus")

// UI 控件
include(":libs:lib_widget")
include(":libs:lib_ninepatch")

// Tab 导航
include(":modules:module_tab")
// 动画（原生动画 + 第三方动画库）
include(":modules:module_anim")
// UI 控件
include(":modules:module_widget")
// 自定义控件
include(":modules:module_widget_custom")
// 第三方控件
include(":modules:module_widget_thirdparty")

// 同步
include(":modules:module_async")
// 后台任务调度
include(":modules:module_scheduler")
// 组件交互
include(":modules:module_component")
// 跨进程通信
include(":modules:module_ipc")
// 系统服务
include(":modules:module_system_service")

// 示例
include(":modules:module_sample")
// 业务功能
include(":modules:module_feature")
// 性能优化
include(":modules:module_performance")
// 多媒体
include(":modules:module_media")
// 机器学习 / AI (TFLite / LiteRT)
include(":modules:module_ml")

// HTTP 网络请求（基础、OkHttp、Retrofit、RxRetrofit、Ktor）
include(":modules:module_http")
// 流式推送 / SSE
include(":modules:module_sse")
// Markdown 渲染 / 代码高亮 / 流式打字机 / AI 聊天
include(":modules:module_markdown")
// Socket 长连接（WebSocket & TCP Socket）
include(":modules:module_socket")
// MQTT
include(":modules:module_mqtt")
// 蓝牙通信
include(":modules:module_bluetooth")

// 事件总线
include(":modules:module_event")
// 图片加载
include(":modules:module_image_loader")

// Kotlin 特性
include(":modules:module_kotlin")
// 响应式编程（Flow / RxJava 操作符对照）
include(":modules:module_reactive")
// Jetpack 组件库
include(":modules:module_jetpack")
// 数据库
include(":modules:module_database")
// 存储
include(":modules:module_storage")
// 依赖注入
include(":modules:module_di")
// MVP，MVI，MVVM，Mavericks 架构
include(":modules:module_arch")

// Compose
include(":modules:module_compose")

// 性能优化基线与基准测试 (Macrobenchmark & Baseline Profile)
include(":benchmarks")
// 自定义 Lint 规则模块（测试命名规范等），通过 build-logic 的 lintChecks 注入各 Android 模块
include(":lint")

val enableFlutter = providers.gradleProperty("enableFlutter")
    .orElse("true")
    .get()
    .toBoolean()
if (enableFlutter) {
    apply(from = "flutter.gradle.kts")
}
include(":modules:module_flutter")

