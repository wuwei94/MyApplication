# `:basic:basic_sync`

> 对齐 Google [Now in Android](https://github.com/android/nowinandroid) `:sync` 职责：
> 声明式后台增量数据同步。提供 `Synchronizer` 驱动的平行 Worker 实现——
> `ServiceLocatorSyncWorker`（运行时查单例）与 `HiltSyncWorker`（`@HiltWorker` 构造注入），
> 经 `SyncWorkerFactory` 一并接入 WorkManager `Configuration.Provider`。
> 游标持久化见 `:basic:basic_datastore`（Proto DataStore）。
