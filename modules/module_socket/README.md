# `:modules:module_socket`

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
    :modules:module_socket[module_socket]:::android-library
  end
  subgraph :basic
    direction TB
    :basic:basic_lib[basic_lib]:::android-library
    :basic:basic_shared[basic_shared]:::android-library
  end
  subgraph :libs
    direction TB
    :libs:lib_netty[lib_netty]:::android-library
    :libs:lib_websocket_java[lib_websocket_java]:::android-library
    :libs:lib_websocket_okhttp[lib_websocket_okhttp]:::android-library
  end

  :basic:basic_shared -.-> :basic:basic_lib
  :modules:module_socket -.-> :basic:basic_lib
  :modules:module_socket -.-> :basic:basic_shared
  :modules:module_socket -.-> :libs:lib_netty
  :modules:module_socket -.-> :libs:lib_websocket_java
  :modules:module_socket -.-> :libs:lib_websocket_okhttp

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
