import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:scroll_to_index/scroll_to_index.dart';

/// ScrollToIndex — 按索引滚动定位
///
/// 核心机制与避坑点：
/// 1. 控制器契约：列表项需包 `AutoScrollTag` 并绑定 `index`，`scrollToIndex` 才能定位。
/// 2. 对齐参数：`preferPosition` 控制目标项停在视口顶部还是底部。
///
/// 官方参考：
/// https://pub.dev/packages/scroll_to_index
class ScrollToIndexDemoPage extends BasicLayoutPage {
  const ScrollToIndexDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<ScrollToIndexDemoPage> createState() =>
      _ScrollToIndexDemoPageState();
}

class _ScrollToIndexDemoPageState
    extends BasicLayoutPageState<ScrollToIndexDemoPage> {
  static const int _itemCount = 50;

  final AutoScrollController _controller = AutoScrollController();

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  List<String> buildList() => const <String>[
        '1. 滚动到索引 0',
        '2. 滚动到索引 10',
        '3. 滚动到索引 25',
        '4. 滚动到索引 49',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _scrollToIndex(0);
      case 1:
        _scrollToIndex(10);
      case 2:
        _scrollToIndex(25);
      case 3:
        _scrollToIndex(49);
    }
  }

  Future<void> _scrollToIndex(int index) async {
    await _controller.scrollToIndex(
      index,
      preferPosition: AutoScrollPosition.begin,
    );
  }

  @override
  Widget buildPreview() {
    return ListView.builder(
      controller: _controller,
      itemCount: _itemCount,
      padding: const EdgeInsets.symmetric(vertical: 8),
      itemBuilder: (BuildContext context, int index) {
        return AutoScrollTag(
          key: ValueKey<int>(index),
          controller: _controller,
          index: index,
          child: Card(
            margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
            color: index % 10 == 0 ? Colors.blue.shade50 : null,
            child: ListTile(
              title: Text('Item #$index'),
              subtitle: Text(index % 10 == 0 ? '锚点索引' : '普通列表项'),
            ),
          ),
        );
      },
    );
  }
}
