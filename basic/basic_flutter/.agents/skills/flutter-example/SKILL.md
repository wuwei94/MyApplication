---
name: "flutter-example"
description: "为 basic_flutter 项目生成统一的 Example/Route 页面骨架，复杂场景可扩展到 Example/Page"
metadata:
  model: "998Code/gpt-5.4"
  last_modified: "Sun, 15 Mar 2026 00:00:00 GMT"

---
# flutter-example

## Goal
为 `basic_flutter` 项目生成风格统一的示例页面骨架，并按场景选择合适模式：
- 默认模式：`XxxExample -> XxxRoute`
- 扩展模式：复杂场景允许 `XxxExample -> XxxPage`

核心原则：外层 `Example` 负责路由入口和轻量组装，内层 `Route` 或 `Page` 负责真实页面内容。

## Instructions

### 1. 先选目录
- 新页面优先放到已有分类目录，不要随意在 `lib/` 顶层新增页面文件。
- 目录选择规则：
  - `lib/demos/packages/`：三方包、平台能力、工具封装演示
  - `lib/demos/examples/`：基础示例、完整小型示例应用
  - `lib/demos/state_manager/`：`provider`、`bloc`、`riverpod` 等状态管理示例
  - `lib/demos/storage/`：本地存储相关示例，如 `shared_preferences`、`hive`、`secure_storage`
  - `lib/demos/network/`：网络请求相关示例，如 `dio`、`http`
- 如果一个新示例已经明显属于现有分组，就放到对应分组下，不要新建平行目录。

### 2. 先选骨架
- 按复杂度选择模板：
  - 默认情况：`XxxExample -> XxxRoute`
  - 带本地状态类：`XxxExample -> Stateful XxxRoute`
  - 复杂场景：`XxxExample -> XxxPage`
- 同一个示例内保持一种清晰模式，不要混入多套入口风格。

### 3. 外层 Example 规则
- 页面文件命名使用 `xxx_example.dart`。
- 对外入口组件默认命名为 `XxxExample`，优先使用 `StatelessWidget`。
- 默认只负责返回对应的 `XxxRoute`。
- 只有在复杂场景下，才让 `XxxExample` 返回 `XxxPage`。
- 允许承担轻量组装职责，例如：
  - provider / bloc / controller 注入
  - `ScreenUtilInit` 之类的外层包裹
  - observer 注册与恢复
  - 必要的生命周期桥接
- 不要把具体业务 UI、复杂布局、列表渲染和页面交互细节塞进 `XxxExample`。
- 如果外层需要管理注入层生命周期、恢复全局设置或注册/注销 observer，允许 `XxxExample` 使用 `StatefulWidget`。

```dart
class ToastExample extends StatelessWidget {
  const ToastExample({super.key});

  @override
  Widget build(BuildContext context) {
    return const ToastRoute(title: 'Toast Example');
  }
}
```

### 4. 内层 Route/Page 规则
- `XxxRoute`：单文件内承载 `Scaffold` 的页面组件。
- `XxxPage`：复杂场景下拆到 `pages/` 目录中的真实页面组件。
- 默认优先生成 `XxxRoute`；只有在页面明显变复杂时，才扩展为 `XxxPage`。
- 无本地状态时用 `StatelessWidget`；需要 `initState`、`setState`、控制器生命周期时改用 `StatefulWidget`。
- 有 `await` 后再更新 UI、弹 `SnackBar`、`setState` 或访问上下文时：
  - 在 `State` 里优先使用 `if (!mounted) return;`
  - 只有依赖 `BuildContext` 时使用 `if (!context.mounted) return;`
- 常规页面优先保持这个骨架：

```dart
class XxxRoute extends StatelessWidget {
  const XxxRoute({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: getBody(),
      floatingActionButton: getFAB(),
    );
  }

  Widget getBody() {
    return const Center();
  }

  Widget getFAB() {
    return FloatingActionButton(
      onPressed: null,
      child: const Icon(Icons.add),
    );
  }
}
```

- 如果页面不需要 FAB，可以删除 `floatingActionButton` 和 `getFAB()`，不要为了统一而保留空实现。
- 如果页面明显变复杂，就继续拆小组件或拆目录，但保持入口模式一致。

### 5. 复杂示例及时拆目录
- 当示例包含状态管理、controller、notifier、observer、binding、可复用页面组件时，不要把所有内容塞进一个 `*_example.dart`。
- 应按现有项目风格拆分，例如：
  - `pages/`
  - `controllers/`
  - `notifiers/`
  - `cubits/`
  - `providers/`
  - `observers/`
  - `bindings/`
- `*_example.dart` 优先只保留路由入口、注入层、包裹层和必要的轻量生命周期逻辑。
- 可参考：
  - `lib/demos/state_manager/provider/`
  - `lib/demos/state_manager/bloc/`
  - `lib/demos/state_manager/riverpod/`
  - `lib/demos/examples/getx/`

### 6. 命名与 helper 约定
- 类名、文件名、路由标题保持一致语义，例如：
  - `ToastExample` / `ToastRoute` / `toast_example.dart`
  - `CounterProviderExample` / `CounterProviderPage` / `counter_provider_example.dart`
- `title` 默认使用 `Xxx Example` 风格，和现有示例保持一致。
- `subtitle` 默认与当前分组现有风格保持一致；若无额外说明，可与 `title` 主语义一致。
- 同一个文件内保持一种 helper 命名风格，不要混用 `getBody()` 和 `_buildBody()`。
- 默认骨架优先使用：
  - `getBody()`
  - `getFAB()`
- 如果 FAB 逻辑依赖上下文，优先写成 `Widget getFAB(BuildContext context)`。
- 只有在页面明显更复杂、且整个文件已经采用私有 build helper 风格时，才使用 `_buildBody()`、`_buildFab()`。

### 7. 路由接入规则
- 新增路由时，path 按现有分组前缀组织：
  - example: `/example/...`
  - packages: `/package/...`
  - state management: `/state/...`
  - storage: `/storage/...`
  - network: `/network/...`
- 多单词 path 优先使用 kebab-case，例如：
  - `/package/shared-preferences`
  - `/package/screen-util`
  - `/storage/secure-storage`
- 路由项统一使用 `CatalogItem`。
- `routeBuilder` 应直接返回 `const XxxExample()`。
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
- 默认禁止在普通 routed page 外再包一层 `MaterialApp`、`GetMaterialApp`。

### 9. 保持现有导航层级
- 这个项目的导航链路是固定三层：
  - `HomePage` 用 `Navigator.push` 打开分组列表页
  - `DemoCatalogPage` 用 `AppNavigator.pushPath(item.path)` 进入最终示例页
  - 最终示例页由当前启用的路由器提供
- 新增示例时不要随意改掉这三层结构，也不要把首页分组入口和最终示例页混成同一级。

### 10. 新增页面时同步文件
- 如果只是向现有分组新增页面，通常只需要更新对应分组下的 `lib/demos/*/catalog.dart`。
- 如果新增的是首页一级分组，还要同步更新：
  - `lib/app/catalog/demo_catalog.dart`
  - 必要时检查 `lib/app/router/app_router.dart`

### 11. 完成后验证
- 优先运行：
  - `fvm flutter analyze`
  - `fvm flutter test`
- 如果改动了路由，至少验证：
  - 首页分组入口正常
  - `DemoCatalogPage` 分组列表入口正常
  - 最终页面跳转正常

## Constraints
- 默认优先生成 `XxxExample -> XxxRoute`。
- 只有在复杂场景下，才扩展为 `XxxExample -> XxxPage`。
- `XxxExample` 不承载复杂页面 UI 和业务细节，但允许承载轻量注入、包裹层和必要生命周期逻辑。
- 只有在确实需要本地状态时，才让内层 `XxxRoute` 或 `XxxPage` 使用 `StatefulWidget`。
- 只有在确实需要注入层生命周期管理、observer 恢复、全局包装恢复时，才让外层 `XxxExample` 使用 `StatefulWidget`。
- 保持和现有 `lib/demos/packages/toast_example.dart`、`lib/demos/packages/notification_example.dart`、`lib/demos/state_manager/provider/counter_provider_example.dart` 一致的组织方式。
