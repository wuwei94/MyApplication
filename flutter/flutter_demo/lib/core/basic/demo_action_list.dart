import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic_demo_dimens.dart';

/// 操作列表 — 对应 Android `BasicControlActivity` 的 RecyclerView 操作区。
///
/// 约定：
/// 1. 文案统一 `"N. 动词短语"`。
/// 2. 所有演示行为必须由列表项触发，禁止在上方展示区伪造业务入口。
/// 3. [height] 有值时固定高度并内部滚动（上展示 + 下操作双区）；为空时撑满父级。
class DemoActionList extends StatelessWidget {
  const DemoActionList({
    super.key,
    required this.items,
    required this.onTap,
    this.height,
  });

  /// 操作项文案列表。
  final List<String> items;

  /// 点击回调：回传下标与文案，子类用 `switch (position)` 直调示例方法。
  final void Function(int position, String label) onTap;

  /// 固定高度；双区布局传 `BasicDemoDimens.operationHeight`。
  final double? height;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    final Widget list = ListView.separated(
      padding: EdgeInsets.zero,
      itemCount: items.length,
      separatorBuilder: (BuildContext context, int index) => const Divider(
        height: 1,
        thickness: 1,
      ),
      itemBuilder: (BuildContext context, int index) {
        final String label = items[index];
        return Material(
          color: theme.colorScheme.surface,
          child: ListTile(
            dense: true,
            title: Text(
              label,
              style: const TextStyle(fontSize: BasicDemoDimens.fontTitle),
            ),
            onTap: () => onTap(index, label),
          ),
        );
      },
    );

    if (height != null) {
      return SizedBox(height: height, child: list);
    }
    return list;
  }
}
