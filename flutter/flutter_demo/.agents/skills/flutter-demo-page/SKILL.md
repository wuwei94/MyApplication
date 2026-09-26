---
name: "flutter-demo-page"
description: "flutter_demo 项目专用：生成统一的 Basic*Page（BasicControl/Response/Layout/Image）示例页骨架，叶子 demo 使用 xxx_demo.dart 命名，并按当前目录惯例与 CatalogEntry 架构接入。只负责页面骨架与目录组织；动画、状态管理、网络等具体技术实现请使用对应的官方 flutter-* skill"

---
# flutter-demo-page

> 本 Skill 仅覆盖 `flutter_demo` 项目的页面骨架与 Catalog 接入惯例。动画、状态管理、网络、主题等具体技术实现交给对应的官方 `flutter-*` skill。

## Goal
为 `flutter_demo` 项目生成风格统一的 `Basic*Page` demo 页面骨架，并接入当前 `Catalog First` 架构。

默认模式：
- `XxxDemoPage extends Basic*Page`（见 Instructions 第 0 节）
- 叶子 demo 按当前目录惯例放置：
  - 顶层分组示例：`lib/demos/network/dio_demo.dart`
  - 子分组示例：`lib/demos/layout/containers/align_demo.dart`

复杂场景：
- 允许在 demo 目录下继续拆 `pages/`、`controllers/`、`providers/`、`cubits/`、`bindings/` 等子目录
- `xxx_demo.dart` 继续作为对外入口

## Instructions

### 0. 先选骨架基类（强制）

叶子示例页统一继承 `package:flutter_demo/core/basic/basic.dart` 页面族，对齐 Android `BasicControlActivity`：

| 基类 | 适用 | 样板 |
|------|------|------|
| `BasicResponsePage` | API / IO / 系统能力（默认） | `lib/demos/network/http_demo.dart` |
| `BasicControlPage` | 纯操作 + Toast/状态反馈 | `lib/demos/packages/utils/toast_demo.dart` |
| `BasicLayoutPage` | 布局画布 / 图表 / 参数预览 | `lib/demos/layout/containers/align_demo.dart` |
| `BasicImagePage` | 图片 / 动画 / 媒体预览 | 参见 image/video 分组 |

契约：

- `List<String> buildList()` 返回 `"N. 动词短语"`
- `void onRecyclerClick(int position, String label)` 内 `switch (position)` 直调示例方法
- `BasicResponsePage` 必须 `showDescription`；日志 `appendLog('→ …'|'✓ …'|'✗ …')`，高频 `updateLog`
- 纯布局预览可不实现 `buildList`
- 完整小型应用（getx_app、状态管理计数应用）允许自带壳，属例外

简单页合并为单类：

```dart
class ToastDemoPage extends BasicControlPage {
  const ToastDemoPage({super.key, required super.title});

  @override
  BasicControlPageState<ToastDemoPage> createState() => _ToastDemoPageState();
}

class _ToastDemoPageState extends BasicControlPageState<ToastDemoPage> {
  @override
  List<String> buildList() => const ['1. 显示短 Toast'];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        showToast('show Toast');
    }
  }
}
```

### 1. 先选目录
- 新页面优先放到已有分类目录，不要随意在 `lib/` 顶层新增页面文件。
- 目录选择规则：
  - `lib/demos/packages/`：三方包、平台能力、工具封装演示
  - `lib/demos/basics/`：基础示例和完整小型示例应用，例如 `counter`、`getx_app`
  - `lib/demos/video/`：视频播放相关示例，如 `video_player`、`chewie`
  - `lib/demos/state_management/`：`provider`、`bloc`、`riverpod` 等状态管理示例
  - `lib/demos/storage/`：本地存储相关示例，如 `shared_preferences`、`hive`、`secure_storage`
  - `lib/demos/network/`：网络请求相关示例，如 `dio`、`http`
  - `lib/demos/animation/`：动画资源和播放示例
  - `lib/demos/layout/`：布局和交互类示例
  - `lib/demos/showcase/`：展示类示例
- 如果一个新示例已经明显属于现有分组，就放到对应分组下，不要新建平行目录。
- 普通叶子 demo 优先直接放在分组目录或子分组目录下，不要再额外嵌套一层同名目录。
- 如果某个分类已经有稳定的子目录组织，就沿用现有结构，例如 `lib/demos/basics/counter/counter_demo.dart`、`lib/demos/state_management/provider/provider_demo.dart`。

### 2. 先选组织粒度
- 默认：`XxxDemoPage extends Basic*Page` 单类（State 写逻辑）。
- 需要注入层 / observer 生命周期：保留 `XxxDemoPage -> XxxDemoView`，View 继承 `Basic*Page` 或使用其布局契约。
- 复杂场景：`xxx_demo.dart` 作为入口，再拆 `pages/` 等子目录。
- 同一个示例内保持一种清晰模式，不要混入多套入口风格。

### 3. 对外入口规则
- 叶子 demo 页面文件统一使用 `xxx_demo.dart`。
- 对外入口组件命名为 `XxxDemoPage`，继承对应 `Basic*Page`。
- 禁止把演示行为只挂在 `FloatingActionButton` 上；触发入口统一是 `buildList()` 操作项。
- 允许 `XxxDemoPage` 承担轻量组装（注入 / observer），此时才拆 `XxxDemoView`。

### 4. 内层页面规则
- `XxxDemoPage` 的 `State` 直接实现 `buildList` / `onRecyclerClick` 与目标 API 调用。
- 预览型页面覆写 `buildPreview()`；图片型覆写 `buildPreview()` / 调用 `showImage`。
- 有 `await` 后再更新 UI 时：`State` 里优先 `if (!mounted) return;`；仅依赖 `BuildContext` 时用 `if (!context.mounted) return;`。
- 不要自造 Scaffold 双区结构；基类已提供上展示 / 下操作布局。

### 5. 复杂示例及时拆目录
- 当示例包含状态管理、controller、notifier、observer、binding、可复用页面组件时，不要把所有内容塞进一个 `xxx_demo.dart`。
- 应按现有项目风格拆分，例如：
  - `pages/`
  - `controllers/`
  - `notifiers/`
  - `cubits/`
  - `providers/`
  - `observers/`
  - `bindings/`
- `xxx_demo.dart` 优先只保留对外入口、注入层、包裹层和必要的轻量生命周期逻辑。
- 只有在 demo 本身确实有多文件支撑内容时，才保留额外目录；不要为了单页 demo 再嵌套一层同名目录。
- 可参考：
  - `lib/demos/state_management/provider/`
  - `lib/demos/state_management/bloc/`
  - `lib/demos/state_management/riverpod/`
  - `lib/demos/basics/getx_app/`

### 6. 命名与 helper 约定
- 类名、文件名、路由标题保持一致语义，例如：
  - `ToastDemoPage` / `packages/utils/toast_demo.dart`
  - `HttpDemoPage` / `network/http_demo.dart`
  - `AlignDemoPage` / `layout/containers/align_demo.dart`
- `title` 默认与 demo 名称语义一致，例如 `Toast`、`SharedPreferences`、`Counter Example`。
- `subtitle` 默认与当前分组现有风格保持一致；若无额外说明，可与 `title` 主语义一致。
- 逻辑方法名直接表达 API 动作（`_handleGet`、`_incrementCounter`），不再使用 `getBody()`/`getFAB()` 骨架方法。

### 7. Catalog 接入规则
- 普通 demo 不手写额外 app 路由，统一通过 `catalog.dart` 接入。
- 路由项统一使用 `CatalogEntry`。
- 叶子页面使用 `CatalogEntry.page(...)`。
- 子分组使用 `CatalogEntry.catalog(...)`。
- `path` 统一写相对路径：
  - 顶层分组相对根目录，例如 `basics`、`layout`、`network`
  - 子分组和页面相对父分组目录，例如 `containers`、`dio`、`shared-preferences`
- 多单词 path 优先使用 kebab-case，例如：
  - `shared-preferences`
  - `screen-util`
  - `secure-storage`
- `pageBuilder` 应直接返回 `const XxxDemoPage(...)`。
- 需要包示例说明时，可在文件顶部保留这种注释风格：

```dart
/// Toast
/// https://pub.dev/packages/fluttertoast
```

### 8. 项目约定
- 只用 package import。
- 优先使用 `const`、`final`、强类型、显式返回类型。
- 不使用 `print`；如需日志，使用 `lib/core/utils/logger/`。
- 尽量延续周边文件已有的中英文风格。
- 异步逻辑里遵守 `use_build_context_synchronously` 规则，不要在缺少 mounted 检查时直接使用上下文。
- 写 package 示例前，先检查 `lib/core/utils/` 是否已有封装；若已有，优先复用项目统一封装，而不是重复直接调用第三方包 API。
- 常见复用位置包括：
  - `lib/core/utils/ui/toast.dart`
  - `lib/core/utils/ui/notification.dart`
  - `lib/core/utils/storage/shared_preferences.dart`
  - `lib/core/utils/logger/logger.dart`
- 默认禁止在普通 demo page 外再包一层 `MaterialApp`、`GetMaterialApp`。
- 只有像 `lib/demos/basics/getx_app/` 这种独立实验应用，才允许自带 `GetMaterialApp`。

### 9. 保持现有导航层级
- 这个项目的导航链路是固定三层：
  - `AppHome` 展示首页顶层分组
  - `CatalogPage` 展示分组列表
  - `CatalogPage` 内通过 `AppNavigator.pushPath(item.path)` 进入最终示例页
  - 最终示例页由当前启用的路由器提供
- 新增示例时不要随意改掉这三层结构，也不要把首页分组入口和最终示例页混成同一级。

### 10. 新增页面时同步文件
- 如果只是向现有分组新增页面，通常只需要更新对应分组下的 `lib/demos/*/catalog.dart`。
- 如果新增的是首页一级分组，还要同步更新：
  - `lib/catalog/registry/catalog_registry.dart`
  - 必要时检查 `lib/app/router/app_router_config.dart`

### 11. 完成后验证
- 优先运行：
  - `fvm flutter analyze`
  - `fvm flutter test`
- 如果改动了路由，至少验证：
  - 首页分组入口正常
  - `CatalogPage` 分组列表入口正常
  - 最终页面跳转正常

## Constraints
- 默认生成 `XxxDemoPage extends Basic*Page`，按第 0 节选型。
- 只有在复杂场景下，才扩展为 `xxx_demo.dart + pages/` 等子目录。
- `XxxDemoPage` / 其 State 直接展示目标库 API；禁止产品化假业务 UI。
- 纯布局预览不强制 `buildList`；API 演示必须 `showDescription` + 规范日志。
- 保持和现有 `lib/demos/packages/utils/toast_demo.dart`、`lib/demos/network/http_demo.dart`、`lib/demos/storage/shared_preferences_demo.dart`、`lib/demos/layout/containers/align_demo.dart` 一致的组织方式。
