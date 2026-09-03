# `:modules:module_http`

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
    :modules:module_http[module_http]:::android-library
  end
  subgraph :basic
    direction TB
    :basic:basic_lib[basic_lib]:::android-library
    :basic:basic_repo[basic_repo]:::android-library
    :basic:basic_server[basic_server]:::android-library
    :basic:basic_shared[basic_shared]:::android-library
  end
  subgraph :libs
    direction TB
    :libs:lib_httpurl[lib_httpurl]:::android-library
    :libs:lib_ktor[lib_ktor]:::android-library
    :libs:lib_nanohttpd[lib_nanohttpd]:::android-library
    :libs:lib_netty[lib_netty]:::android-library
    :libs:lib_okhttp[lib_okhttp]:::android-library
    :libs:lib_retrofit[lib_retrofit]:::android-library
    :libs:lib_retrofit_rx[lib_retrofit_rx]:::android-library
    :libs:lib_rx_download[lib_rx_download]:::android-library
    :libs:lib_rx_request[lib_rx_request]:::android-library
    :libs:lib_rx_upload[lib_rx_upload]:::android-library
    :libs:lib_volley[lib_volley]:::android-library
    :libs:lib_websocket_java[lib_websocket_java]:::android-library
  end

  :basic:basic_repo -.-> :basic:basic_lib
  :basic:basic_repo -.-> :basic:basic_shared
  :basic:basic_repo --> :libs:lib_okhttp
  :basic:basic_repo --> :libs:lib_retrofit
  :basic:basic_repo --> :libs:lib_retrofit_rx
  :basic:basic_server -.-> :basic:basic_lib
  :basic:basic_server -.-> :basic:basic_shared
  :basic:basic_server -.-> :libs:lib_nanohttpd
  :basic:basic_server -.-> :libs:lib_netty
  :basic:basic_server -.-> :libs:lib_websocket_java
  :basic:basic_shared -.-> :basic:basic_lib
  :libs:lib_retrofit --> :libs:lib_okhttp
  :libs:lib_retrofit_rx --> :libs:lib_retrofit
  :libs:lib_rx_download --> :libs:lib_retrofit_rx
  :libs:lib_rx_request --> :libs:lib_retrofit_rx
  :libs:lib_rx_upload --> :libs:lib_retrofit_rx
  :modules:module_http -.-> :basic:basic_lib
  :modules:module_http -.-> :basic:basic_repo
  :modules:module_http -.-> :basic:basic_server
  :modules:module_http -.-> :basic:basic_shared
  :modules:module_http -.-> :libs:lib_httpurl
  :modules:module_http -.-> :libs:lib_ktor
  :modules:module_http -.-> :libs:lib_okhttp
  :modules:module_http -.-> :libs:lib_retrofit
  :modules:module_http -.-> :libs:lib_retrofit_rx
  :modules:module_http -.-> :libs:lib_rx_download
  :modules:module_http -.-> :libs:lib_rx_request
  :modules:module_http -.-> :libs:lib_rx_upload
  :modules:module_http -.-> :libs:lib_volley

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
