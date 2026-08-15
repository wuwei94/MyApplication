# network_dio

MyApplication 的 Flutter Demo Catalog 使用的独立 Dio 网络封装库。

公共 API 从 `package:network_dio/network_dio.dart` 导出，不依赖 `basic_flutter` 或 `network_http`。

## 核心契约

- 支持 GET、POST、PUT、PATCH、DELETE 与 form/json/raw 请求体。
- 标准业务响应字段为 `errorCode`、`errorMsg`、`data`，成功码为 `0`。
- 标准业务信封使用 JSON 响应，`errorCode` 按整数类型读取。
- `decoder` 只转换 `data`；非零业务码默认保留在 `NetworkResponse` 中，调用 `requireSuccess()` 会通过 `ServerResultException` 转为 `NetworkException`。
- `NetworkException` 与 Android `ApiException` 对齐：HTTP 错误使用状态码，连接/超时/SSL/解析错误分别使用 `1001`/`1002`/`1003`/`1004`，其他错误使用 `1000`。
- Dio 取消继续向上传播，不包装为 `NetworkException`。
- `timeout` 统一配置连接、接收和发送超时；未指定时保留注入 Dio 的原配置。
- 日志由调用方通过 Dio `Interceptor` 配置，本包不改写日志内容，也不额外脱敏。

认证、Token 刷新、Cookie、缓存、重试、重定向和代理由调用方通过 Dio `Interceptor` 或 adapter 配置。本包不持有业务 Token，不包含上传与下载任务编排。
