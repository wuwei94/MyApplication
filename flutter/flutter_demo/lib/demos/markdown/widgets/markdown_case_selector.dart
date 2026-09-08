import 'package:flutter/material.dart';

/// 顶部横滑案例选择器（Markdown 示例页通用）
///
/// 内容型页面（长文档 / 大段代码预览）采用顶部切换而非 Android
/// `BasicLayoutActivity` 底部 300dp 操作台，将几乎全部纵向空间留给内容，
/// 符合 Flutter 多案例预览页的通用交互形态。
class MarkdownCaseSelector extends StatelessWidget {
  const MarkdownCaseSelector({
    super.key,
    required this.titles,
    required this.index,
    required this.onSelected,
  });

  /// 案例标题列表（文案与 Android `buildList()` 保持一致）
  final List<String> titles;

  /// 当前高亮下标（-1 表示不持久高亮，如动作类操作项）
  final int index;

  /// 选中回调（参数为列表下标）
  final ValueChanged<int> onSelected;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 48,
      child: ListView.separated(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
        scrollDirection: Axis.horizontal,
        itemCount: titles.length,
        separatorBuilder: (BuildContext context, int index) =>
            const SizedBox(width: 8),
        itemBuilder: (BuildContext context, int i) {
          return ChoiceChip(
            label: Text(
              titles[i],
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
            labelStyle: const TextStyle(fontSize: 12),
            selected: index == i,
            onSelected: (bool selected) {
              if (selected) onSelected(i);
            },
          );
        },
      ),
    );
  }
}
