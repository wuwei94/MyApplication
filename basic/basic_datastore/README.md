# `:basic:basic_datastore`

> 对齐 Google [Now in Android](https://github.com/android/nowinandroid) `:core:datastore` 职责：
> 统一本地偏好与游标存储层。采用 Proto DataStore（Protobuf schema）集中管理增量同步版本游标（`ChangeListVersions`），并通过 `Flow` 向上层暴露响应式数据流。
