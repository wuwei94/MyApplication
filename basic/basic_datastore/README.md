# `:basic:basic_datastore`

> 对齐 Google [Now in Android](https://github.com/android/nowinandroid) `:core:datastore` 职责：
> 统一本地 Key-Value 键值与偏好存储层。采用 Tencent MMKV（基于 mmap 内存映射）集中管理增量同步版本游标（`ChangeListVersions`）及键值配置，并通过 `StateFlow` 向上层暴露无损响应式数据流。
