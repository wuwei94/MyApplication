import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic_control_page.dart';
import 'package:flutter_demo/core/basic/demo_action_list.dart';

/// 布局/视图容器类示例页面基类 — 对应 Android `BasicLayoutActivity`。
///
/// 布局结构：
/// - 上方展示：动态视图画布（[buildPreview]）
/// - 下方列表：操作列表（可选；[buildList] 驱动参数调节 / 状态切换）
///
/// 约定与规范：
/// 1. 上方区域纯粹作为组件渲染画布，禁止在页面内自造假业务容器。
/// 2. 有操作项时，[buildList] 专职驱动参数调节 / 状态切换。
/// 3. 纯布局预览页可不建 [buildList]，画布撑满主体。
abstract class BasicLayoutPage extends BasicControlPage {
  const BasicLayoutPage({super.key, required super.title});
}

/// [BasicLayoutPage] 的状态基类。
abstract class BasicLayoutPageState<T extends BasicLayoutPage>
    extends BasicControlPageState<T> {
  /// 上方预览画布。
  Widget buildPreview();

  @override
  Widget buildBody() {
    final List<String> items = buildList();
    if (items.isEmpty) {
      return buildPreview();
    }
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        Expanded(child: buildPreview()),
        DemoActionList(
          items: items,
          onTap: onRecyclerClick,
          height: operationHeight,
        ),
      ],
    );
  }
}
