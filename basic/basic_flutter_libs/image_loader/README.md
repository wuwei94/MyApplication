# image_loader

MyApplication 的 Flutter Demo Catalog 使用的图片加载封装库，与 Android `lib_imageloader` 结构对齐。

公共 API 从 `package:image_loader/image_loader.dart` 导出，不依赖 `basic_flutter`。

## 核心契约

- `IImageLoader` 定义统一接口（`load` / `radius` / `round` / `provider` / `clear`），当前内核为 `CachedNetworkImageLoader`（基于 cached_network_image）。
- 业务侧统一通过 `ImageLoader` 门面调用，切换内核只替换 `ImageLoader.kernel`，调用方零改动。
- `provider` 返回 `ImageProvider`，可直接用于 `CircleAvatar`、`FadeInImage`、`Hero` 等需要 ImageProvider 的组件。
- 占位图与错误图提供默认实现，调用方可整体替换。
- Flutter 的图片生命周期由 Widget 树自动管理，不需要 Android 侧的 pauseRequests / resumeRequests。
- `extended_image` 等查看器场景不属于本包范围，由业务侧单独封装。
