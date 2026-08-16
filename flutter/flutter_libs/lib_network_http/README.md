# lib_network_http

MyApplication 的 Flutter Demo Catalog 使用的独立 package:http 网络封装库。

公共 API 从 `package:lib_network_http/lib_network_http.dart` 导出，不依赖 `flutter_demo` 或 `lib_network_dio`。

## 核心契约

- 支持 GET、POST、PUT、PATCH、DELETE 与 form/json/raw 请求体。
- 标准业务响应字段为 `errorCode`、`errorMsg`、`data`，成功码为 `0`。
- 标准业务信封使用 JSON Content-Type，`errorCode` 按整数类型读取。
- `decoder` 只转换 `data`；非零业务码默认保留在 `NetworkResponse` 中，调用 `requireSuccess()` 会通过 `ServerResultException` 转为 `NetworkException`。
- `NetworkException` 与 Android `ApiException` 对齐：HTTP 错误使用状态码，连接/超时/解析错误分别使用 `1001`/`1002`/`1004`，其他错误使用 `1000`。
- `CancelableOperation` 会触发底层 `AbortableRequest`，取消不包装为 `NetworkException`。
- `enableLogging` 原样输出请求与响应的 Header/Body，不做脱敏；日志策略不属于普通请求契约。

package:http 的 `ClientException` 映射为连接错误 `1001`。认证、Token 刷新、Cookie、缓存、重试、重定向和代理由调用方注入定制 `http.Client` 实现。本包不持有业务 Token，不包含上传与下载任务编排。
