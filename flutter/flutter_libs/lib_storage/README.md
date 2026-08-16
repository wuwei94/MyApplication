# lib_storage

MyApplication 的 Flutter Demo Catalog 使用的键值存储封装库。

公共 API 从 `package:lib_storage/lib_storage.dart` 导出，不依赖 `flutter_demo`。

## 核心契约

- `IStorage` 定义统一接口（`setValue` / `getValue` / `remove` / `clearAll`），业务侧统一通过 `Storage` 门面调用，切换内核只替换 `Storage.kernel`，调用方零改动。
- 默认内核为 `HiveStorage`（基于 hive_flutter，支持任意对象与 `List<String>` 转换）；`SharedPreferencesStorage` 内核适合普通配置类数据，只接受 int / bool / double / String / List\<String\>。
- 安全存储（Token、密钥等敏感数据）不属于本包范围，由业务侧直接使用 flutter_secure_storage。
- 接口保持最小键值语义，不提供响应式监听、JSON 序列化或迁移能力；drift / isar / objectbox 等数据库选型不属于本包范围。
