# `:basic:basic_model`

> 对齐 Google [Now in Android](https://github.com/android/nowinandroid) `:core:model` 设计：
> 纯 Kotlin JVM 模块（零 Android SDK 依赖），承载跨模块共享的核心领域模型（如增量同步游标 `ChangeListVersions`）。
> 位于依赖图最底层，为存储层（`basic_datastore`）、数据仓库层（`basic_repo`）和调度层（`basic_sync`）提供单向模型契约。
