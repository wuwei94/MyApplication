# 示例分组（demos/）

> 每个顶层分组使用 `catalog.dart` 描述目录结构，叶子页面统一使用 `xxx_demo.dart` 命名。

## 分组总览

| 分组 | 说明 | 示例数量 |
|------|------|---------|
| basics | 基础示例与完整小型应用 | 2 |
| layout | 布局与交互类示例 | 14 个子目录 |
| state_management | 状态管理示例 | 4 |
| network | 网络请求示例 | 3 |
| storage | 本地存储示例 | 8 |
| image | 图片加载与选择示例 | 9 |
| animation | 动画资源和播放示例 | 4 |
| video | 视频播放示例 | 2 |
| packages | 常用三方包示例 | 大量 |
| showcase | 杂项展示类示例 | 少量 |

---

## 分组详情

### basics（基础示例）

基础示例与完整小型示例应用。

| 示例 | 说明 |
|------|------|
| counter | 计数器示例 |
| getx_app | GetX 完整应用 |

### layout（布局与交互）

布局与交互类示例，包含 14 个子目录。

| 子目录 | 说明 |
|--------|------|
| adaptive | 自适应布局 |
| async_widgets | 异步组件 |
| asynchronous | 异步编程 |
| containers | 容器组件 |
| decoration | 装饰组件 |
| dialogs | 弹窗组件 |
| flow | 流式布局 |
| interaction | 手势交互 |
| linear | 线性布局 |
| scroll | 滚动组件 |
| slivers | Sliver 组件 |
| stack | 层叠布局 |
| state_primitives | 状态原语 |
| transitions | 过渡动画 |

### state_management（状态管理）

状态管理示例，覆盖主流方案。

| 示例 | 说明 |
|------|------|
| provider | Provider 状态管理 |
| cubit | Cubit 状态管理 |
| bloc | Bloc 状态管理 |
| riverpod | Riverpod 状态管理 |

### network（网络请求）

网络请求与实时通信示例。Dio 与 package:http 共享 `code/message/data` 业务响应、`errorCode/errorMsg/data` JSON 字段、`code/message/cause` 异常和请求体契约，支持 GET/POST/PUT/PATCH/DELETE，并分别使用各自的底层请求取消能力；WebSocket 示例演示双向长连接通信与生命周期。

| 示例 | 说明 |
|------|------|
| dio | 业务 data decoder、完整业务响应展示、原生 LogInterceptor 和 CancelToken；当前示例不输出 Header/Body |
| http | 业务 data decoder、完整业务响应展示、AbortableRequest 与原样请求日志；日志不脱敏 |
| websocket | WebSocket 建立连接、消息双向收发、Echo 回显与连接生命周期状态展示 |

### storage（本地存储）

本地存储示例，覆盖多种存储方案。

| 示例 | 说明 |
|------|------|
| shared_preferences | SharedPreferences 键值存储 |
| secure_storage | FlutterSecureStorage 安全存储 |
| hive | Hive NoSQL 数据库 |
| lib_storage | lib_storage 统一键值存储封装（内核可切换 + 安全存储） |
| drift | Drift SQLite 数据库 |
| isar | Isar NoSQL 数据库 |
| objectbox | ObjectBox 数据库 |
| path_provider | PathProvider 路径管理 |

### image（图片加载与选择）

图片加载与选择相关示例。

| 示例 | 说明 |
|------|------|
| cached_network_image | cached_network_image 网络图片缓存组件 |
| extended_image | ExtendedImage 增强图片组件 |
| lib_image_loader | lib_image_loader 常规网络图加载封装（缓存 + 占位图 + 错误态） |
| flutter_image_compress | 图片压缩 |
| flutter_luban | 图片压缩（鲁班） |
| image_cropper | 图片裁剪 |
| image_picker | ImagePicker 图片选择 |
| photo_view | PhotoView 图片缩放 |
| wechat_picker | WeChatPicker 微信风格选择器 |

### animation（动画播放）

动画资源和播放示例。

| 示例 | 说明 |
|------|------|
| lottie | Lottie 动画播放 |
| pag | PAG 动画播放 |
| svg | SVG 图片渲染 |
| svga | SVGA 动画播放 |

### video（视频播放）

视频播放示例。

| 示例 | 说明 |
|------|------|
| video_player | VideoPlayer 视频播放 |
| chewie_video_player | Chewie 视频播放器 |

### packages（三方包）

常用三方包示例，包含通知、权限、WebView、URL 启动器、屏幕适配、下拉刷新等。

### showcase（杂项展示）

杂项展示类示例，如本地字体。

---

## 新增 Demo 的推荐方式

1. 优先把示例放进已有分类目录，例如 `lib/demos/network/`、`lib/demos/layout/containers/`
2. 叶子示例页面统一使用 `xxx_demo.dart` 命名
3. 在对应分组或子分组的 `catalog.dart` 中通过 `CatalogEntry.page(...)` 或 `CatalogEntry.catalog(...)` 接入
4. 只有在新增顶层分组时，才需要同步更新 `lib/catalog/registry/catalog_registry.dart`
5. 除非用户明确要求，否则不要破坏现有 `flutter.module` 配置，也不要移除 `AppRouterType` 的双路由切换能力
