# `:modules:module_kotlin`

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
    :modules:module_kotlin[module_kotlin]:::android-library
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
  :modules:module_kotlin -.-> :basic:basic_lib
  :modules:module_kotlin -.-> :basic:basic_repo
  :modules:module_kotlin -.-> :basic:basic_shared

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
