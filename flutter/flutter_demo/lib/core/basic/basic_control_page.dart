import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic_demo_dimens.dart';
import 'package:flutter_demo/core/basic/demo_action_list.dart';

/// 纯操作/控制列表类示例页面基类 — 对应 Android `BasicControlActivity`。
///
/// 布局结构：
/// - 操作列表：通过 [buildList] 与 [onRecyclerClick] 触发操作
///
/// 规范约定：
/// 1. 子类实现 [buildList] 构建列表数据源（`"N. 动词短语"`）。
/// 2. 子类实现 [onRecyclerClick] 响应列表项点击，`switch (position)` 直调示例方法。
/// 3. 结果反馈优先 `Toast` / 状态指示，禁止自造复杂对话框或遮罩阻断流程。
abstract class BasicControlPage extends StatefulWidget {
  const BasicControlPage({super.key, required this.title});

  /// 页面标题（AppBar）。
  final String title;
}

/// [BasicControlPage] 的状态基类。
abstract class BasicControlPageState<T extends BasicControlPage> extends State<T> {
  /// 构建操作列表数据源。
  List<String> buildList() => const <String>[];

  /// 列表项点击。
  void onRecyclerClick(int position, String label) {}

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: SafeArea(child: buildBody()),
    );
  }

  /// 页面主体；子类可覆写以插入展示区。
  @protected
  Widget buildBody() {
    return DemoActionList(items: buildList(), onTap: onRecyclerClick);
  }

  /// 双区布局中底部操作区高度。
  @protected
  double get operationHeight => BasicDemoDimens.operationHeight;
}
