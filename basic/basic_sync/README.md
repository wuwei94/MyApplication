# `:basic:basic_sync`

> 对齐 Google [Now in Android](https://github.com/android/nowinandroid) `:core:sync` 设计：
> 独立后台增量同步与任务调度模块。基于 Jetpack WorkManager 统一编排网络约束，配合 DataStore Preferences 维护版本游标，驱动 `basic_repo` 离线优先幂等拉取。
