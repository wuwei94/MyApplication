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
rootProject.name = "My Application"

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

// HTTP 客户端（Kotlin 原生）
include(":libs:lib_ktor")

// 下载 / 上传
//include(":libs:lib_download")
//include(":libs:lib_upload")

// 长连接 / WebSocket
include(":libs:lib_websocket_okhttp")
include(":libs:lib_websocket_java")
include(":libs:lib_netty")

// 服务端
include(":libs:lib_nanohttpd")

// 图片加载
include(":libs:lib_imageloader")

// 消息总线
include(":libs:lib_eventbus")

// UI 控件
include(":libs:lib_widget")
include(":libs:lib_ninepatch")


// UI 控件
include(":modules:module_ui")
// Tab 导航
include(":modules:module_tab")
// 动画
include(":modules:module_anim")
// 自定义控件
include(":modules:module_widget")

// 同步
include(":modules:module_sync")
// 组件交互
include(":modules:module_component")
// 系统能力
include(":modules:module_system")

// 示例
include(":modules:module_sample")
// 业务功能
include(":modules:module_features")

// 网络库
include(":modules:module_network")
// OkHttp
include(":modules:module_okhttp")
// WebSocket
include(":modules:module_websocket")

// 工具库
include(":modules:module_utils")
// 事件总线
include(":modules:module_event")
// 第三方库
include(":modules:module_opensource")

// Kotlin 特性
//include(":modules:module_kotlin")
// Jetpack 组件库
//include(":modules:module_jetpack")
// 架构模式
//include(":modules:module_arch")

// Compose
include(":modules:module_compose")

val enableFlutter = providers.gradleProperty("enableFlutter")
    .orElse("true")
    .get()
    .toBoolean()
if (enableFlutter) {
    apply("flutter.gradle.kts")
}
include(":modules:module_flutter")

