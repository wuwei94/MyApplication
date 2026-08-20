# 代码注释规范

## 基本规则

- 代码注释默认使用中文，标识符、类型名与必要的专有术语除外。
- 注释说明职责、约束或非显而易见的原因，不重复描述代码本身。
- 不得无故删除已有的有效用法、限制和注意事项。

## 类注释

- 使用多行 KDoc。
- 首段采用“职责 + 类型”的简短描述。
- 当类型存在特殊调用约定、安装位置、生命周期、安全或性能限制、非显而易见的副作用时，应补充必要的行为说明、用法或注意事项。
- 当错误用法容易改变行为，且正确用法无法直接从 API 推断时，应提供简短的代码示例。
- 不要求所有类都包含详细说明，避免为了篇幅而重复代码本身。

简短注释：

```kotlin
/**
 * 缓存拦截器
 */
```

包含必要说明的注释：

```kotlin
/**
 * 日志配置
 *
 * addInterceptor：在 response 被调用一次
 * addNetworkInterceptor：在 request 和 response 分别被调用一次
 *
 * 注意：下载文件时不要使用 BODY 级别。
 */
```

包含必要用法的注释：

````kotlin
/**
 * 上传进度拦截器
 *
 * 应注册为网络拦截器。
 *
 * ```kotlin
 * builder.addNetworkInterceptor(InterceptorUploadProgress(listener))
 * ```
 */
````

## 示例页面类注释（Activity 模板）

各功能模块的演示 Activity 应采用结构化 KDoc，便于读者快速了解该技术方案的定位、特性、用法与官方来源：

- **标题与定位**：`<技术/组件名> — <职责定位>`
- **概述**：简明描述框架作用与核心机制。
- **核心特性**：条理化罗列关键能力与技术优势。
- **核心组件/类型**（可选）：说明涉及的关键接口、注解或类型划分。
- **基本用法**：提供标准、小巧、自闭合的关键 API 调用代码块。
- **适用场景**：罗列典型的业务使用场景。
- **参考文档**：官方文档或权威技术链接。

模板示例：

````kotlin
/**
 * DataStore — 数据存储框架
 *
 * DataStore 是 Android Jetpack 提供的数据存储框架，用于替代 SharedPreferences。
 *
 * 两种类型：
 * 1. Preferences DataStore：键值对存储，无需预先定义 schema
 * 2. Proto DataStore：类型安全存储，需要预先定义 Protocol Buffers schema
 *
 * 核心特性：
 * 1. 异步 API：基于 Kotlin 协程与 Flow，完全避免阻塞主线程
 * 2. 类型安全：Proto DataStore 提供编译时类型检查
 * 3. 事务支持：支持数据事务与原子读写，保证数据一致性
 * 4. 自动迁移：支持从 SharedPreferences 自动迁移
 *
 * 基本用法：
 * ```kotlin
 * // Preferences DataStore（顶层单例声明）
 * val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
 *
 * // 读取数据
 * val counterFlow: Flow<Int> = dataStore.data.map { preferences ->
 *     preferences[intPreferencesKey("counter")] ?: 0
 * }
 *
 * // 写入数据
 * dataStore.edit { settings ->
 *     val currentCounter = settings[intPreferencesKey("counter")] ?: 0
 *     settings[intPreferencesKey("counter")] = currentCounter + 1
 * }
 * ```
 *
 * 适用场景：
 * - 替代 SharedPreferences
 * - 键值对数据存储
 * - 需要异步 API 与响应式 Flow 监听的场景
 *
 * https://developer.android.google.cn/topic/libraries/architecture/datastore
 */
````

