# `:modules:module_arch`

## Module dependency graph

<!--region graph-->
```mermaid
---
config:
  layout: elk
  elk:
    nodePlacementStrategy: SIMPLE
---
graph TB
  subgraph :modules
    direction TB
    :modules:module_arch[module_arch]:::android-library
  end
  subgraph :basic
    direction TB
    :basic:basic_lib[basic_lib]:::android-library
    :basic:basic_repo[basic_repo]:::android-library
    :basic:basic_shared[basic_shared]:::android-library
  end
  subgraph :libs
    direction TB
    :libs:lib_okhttp[lib_okhttp]:::android-library
    :libs:lib_retrofit[lib_retrofit]:::android-library
    :libs:lib_retrofit_rx[lib_retrofit_rx]:::android-library
  end

  :basic:basic_repo -.-> :basic:basic_lib
  :basic:basic_repo -.-> :basic:basic_shared
  :basic:basic_repo --> :libs:lib_okhttp
  :basic:basic_repo --> :libs:lib_retrofit
  :basic:basic_repo --> :libs:lib_retrofit_rx
  :basic:basic_shared -.-> :basic:basic_lib
  :libs:lib_retrofit --> :libs:lib_okhttp
  :libs:lib_retrofit_rx --> :libs:lib_retrofit
  :modules:module_arch -.-> :basic:basic_lib
  :modules:module_arch -.-> :basic:basic_repo
  :modules:module_arch -.-> :basic:basic_shared

classDef android-application fill:#CAFFBF,stroke:#000,stroke-width:2px,color:#000;
classDef android-feature fill:#FFD6A5,stroke:#000,stroke-width:2px,color:#000;
classDef android-library fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000;
classDef android-test fill:#A0C4FF,stroke:#000,stroke-width:2px,color:#000;
classDef jvm-library fill:#BDB2FF,stroke:#000,stroke-width:2px,color:#000;
classDef unknown fill:#FFADAD,stroke:#000,stroke-width:2px,color:#000;
```

<details><summary>📋 Graph legend</summary>

```mermaid
graph TB
  application[application]:::android-application
  feature[feature]:::android-feature
  library[library]:::android-library
  jvm[jvm]:::jvm-library

  application -.-> feature
  library --> jvm

classDef android-application fill:#CAFFBF,stroke:#000,stroke-width:2px,color:#000;
classDef android-feature fill:#FFD6A5,stroke:#000,stroke-width:2px,color:#000;
classDef android-library fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000;
classDef android-test fill:#A0C4FF,stroke:#000,stroke-width:2px,color:#000;
classDef jvm-library fill:#BDB2FF,stroke:#000,stroke-width:2px,color:#000;
classDef unknown fill:#FFADAD,stroke:#000,stroke-width:2px,color:#000;
```

</details>
<!--endregion-->

## 功能概述

本模块集中展示 Android 平台主流的架构模式演进，各架构模式在子包下独立管理，便于读者横向对比：

| 架构模式 | 页面 | 核心技术栈 | 核心设计特点 |
|---|---|---|---|
| **MVP** | `MvpActivity` | Contract + Presenter | 经典契约分离，View 与 Presenter 相互持有抽象接口，回调驱动渲染 |
| **MVVM** | `MvvmActivity` | LiveData + UseCase | 数据驱动，生命周期感知，支持协程/RxJava/UseCase 多种加载范式 |
| **MVI** | `MviActivity` | StateFlow + Intent + Effect | 单向数据流（UDF），单一不可变 ViewState，单次副作用由 Effect 通道分发 |
| **Compose MVI** | `ComposeMviActivity` | Jetpack Compose + StateFlow | 现代声明式 UI 下的 MVI 最佳实践，结合 SmartRefresh Compose 下拉刷新 |
| **Mavericks** | `MavericksActivity` | Airbnb Mavericks + MVI | 响应式 MVI 框架，MavericksState 状态自动合并，Async 异步结果包装 |
| **Offline-First (SSOT)** | `OfflineFirstActivity` | Room Flow + Write-Only Sync | **Now in Android 官方推荐架构**：Room 作为唯一数据源，UI 仅监听 Room Flow，网络仅负责写入 Room |

